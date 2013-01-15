package resolucion;

import java.io.File;

import javax.inject.Inject;

import org.joda.time.DateTime;

import play.modules.guice.InjectSupport;
import play.mvc.results.RenderBinary;
import properties.FapProperties;

import reports.Report;
import services.GestorDocumentalService;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import messages.Messages;
import models.Documento;
import models.LineaResolucion;
import models.Registro;
import models.Resolucion;
import models.SolicitudGenerica;

@InjectSupport
public class ResolucionBase {

	@Inject
    public static GestorDocumentalService gestorDocumentalService;
	
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String BODY_REPORT = "reports/resolucion/resolucion.html";
	private final static String TIPO_RESOLUCION = FapProperties.get("fap.aed.tiposdocumentos.resolucion");
	
	public Resolucion resolucion;
	
	public ResolucionBase (Resolucion resolucion) {
		this.resolucion = resolucion;
	}
	
	public static String getHeaderReport() {
		return ResolucionBase.HEADER_REPORT;
	}
	
	public static String getFooterReport() {
		return ResolucionBase.FOOTER_REPORT;
	}
	
	public boolean isPublicable() {
		return false;
	}
	
	public String getBodyReport() {
		return ResolucionBase.BODY_REPORT;
	}
	
	public String getTipoRegistroResolucion() {
		return ResolucionBase.TIPO_RESOLUCION;
	}
	
	public boolean hasAnexoConcedido() {
		return true;
	}
	
	public boolean hasAnexoDenegado() {
		return true;
	}
	
	public boolean hasAnexoExcluido() {
		return true;
	}
	
	/**
	 * Devuelve las solicitudes "posibles" a resolver (lista desde donde se seleccionará)
	 * @return
	 */
	public static java.util.List<SolicitudGenerica> getSolicitudesAResolver () {
		//return SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.estado=?", "iniciada").fetch();
		return SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.estado in('verificado','excluido','desistido')").fetch();
	}
	
	/**
	 * Establece las líneas de resolución a una resolución, en caso de que no existan.
	 * @param idResolucion
	 */
	public static void setLineasDeResolucion (Long idResolucion) {
		Resolucion resolucion = Resolucion.findById(idResolucion);
		if (resolucion.lineasResolucion.size() == 0) {
			// Por cada una de las solicitudes a resolver, añadimos una línea de resolución
			for (SolicitudGenerica sol: getSolicitudesAResolver()) {
				LineaResolucion lResolucion = new LineaResolucion();
				lResolucion.solicitud = sol;
				if (sol.estado.equals(EstadosSolicitudEnum.verificado)) {
					lResolucion.estado = EstadoLineaResolucionEnum.concedida.name();
				} else if (sol.estado.equals(EstadosSolicitudEnum.excluido.name())) {
					lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
				} else {
					lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
				}
				lResolucion.save();
				
				resolucion.lineasResolucion.add(lResolucion);
				resolucion.save();
			}
		}
	}
	
	public void prepararResolucion(Long idResolucion){
		Resolucion resolucion = Resolucion.findById(idResolucion);
        
        validar();
        eliminarBorradorResolucion(resolucion);
        eliminarOficialResolucion(resolucion);
        File borrador = generarBorradorResolucion(resolucion);
        File oficial = generarOficialResolucion(resolucion);
        almacenarEnGestorDocumentalResolucion(resolucion, borrador, oficial);    
        avanzarFase_Borrador(resolucion);
    }
	
	public void validar() {

	}
	
	 public void eliminarBorradorResolucion(Resolucion resolucion){
		if (!Messages.hasErrors()) {
			// Borramos los documentos que se pudieron generar en una llamada
			// previa al metodo, para no dejar basura en la BBDD
			if ((resolucion.registro.borrador != null) && (resolucion.registro.borrador.uri != null)) {
				Documento borradorOld = resolucion.registro.borrador;
				resolucion.registro.borrador = null;
				resolucion.registro.save();
				try {
					gestorDocumentalService.deleteDocumento(borradorOld);
				} catch (Exception e) {
					play.Logger.error("Error eliminando borrador del gestor documental: " + e.getMessage());
				}
			}
		}
	}
	 
	 public void eliminarOficialResolucion(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			if ((resolucion.registro.oficial != null)
					&& (resolucion.registro.oficial.uri != null)) {
				Documento oficialOld = resolucion.registro.oficial;
				resolucion.registro.oficial = null;
				resolucion.registro.save();
				try {
					gestorDocumentalService.deleteDocumento(oficialOld);
				} catch (Exception e) {
					play.Logger.error("Error eliminando documento oficial del gestor documental: " + e.getMessage());
				}
			}
		}
	 }
	 
	 public File generarBorradorResolucion(Resolucion resolucion) {
		File borrador = null;
		borrador = new File(this.getBodyReport());
		if (!Messages.hasErrors()) {
			try {
				play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("resolucion", resolucion);
				borrador = new Report(this.getBodyReport())
						.header(this.getHeaderReport())
						.footer(this.getFooterReport())
						.renderTmpFile(resolucion);
				
				resolucion.registro.borrador = new Documento();
				resolucion.registro.borrador.tipo = getTipoRegistroResolucion();
				resolucion.registro.save();
			} catch (Exception ex2) {
				Messages.error("Error generando el documento borrador");
				play.Logger.error("Error generando el documento borrador: " + ex2.getMessage());
			}
		}

		return borrador;
	 }
	
	 public File generarOficialResolucion(Resolucion resolucion){
		File oficial = null;
		if (!Messages.hasErrors()) {
			try {
				play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("resolucion", resolucion);
				oficial = new Report(this.getBodyReport()).header(this.getHeaderReport()).registroSize().renderTmpFile(resolucion);
				resolucion.registro.oficial = new Documento();
				resolucion.registro.oficial.tipo = getTipoRegistroResolucion();
				resolucion.registro.save();
			} catch (Exception ex2) {
				Messages.error("Error generando el documento oficial");
				play.Logger.error("Error generando el documento oficial: " + ex2.getMessage());
			}
		}
		return oficial;
	 }
	 
	 public void almacenarEnGestorDocumentalResolucion(Resolucion resolucion, File borrador, File oficial){
		if (!Messages.hasErrors()) {
			try {
				gestorDocumentalService.saveDocumentoTemporal(resolucion.registro.borrador, borrador);
				gestorDocumentalService.saveDocumentoTemporal(resolucion.registro.oficial, oficial);
			} catch (Exception e) {
				Messages.error("Error almacenando documentos en el aed");
				play.Logger.error("Error almacenando documentos en el aed: " + e.getMessage());
			}
		}
	 }
	 
	public void avanzarFase_Borrador(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.preparada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Preparada(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.pendienteFirmarJefeServicio.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_PendienteFirmarJefeServicio(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.firmadaJefeServicio.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_FirmadaJefeServicio(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.pendienteFirmarDirector.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_PendienteFirmarDirector(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.firmada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Firmada(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.registrada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Registrada(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.publicada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Publicada(Resolucion resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.finalizada.name();
			resolucion.save();
		}
	}
	
	
}
