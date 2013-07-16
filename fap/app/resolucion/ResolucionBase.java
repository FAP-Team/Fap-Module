package resolucion;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;

import config.InjectorConfig;
import controllers.fap.ResolucionControllerFAP;

import platino.FirmaUtils;
import play.db.jpa.JPA;
import play.modules.guice.InjectSupport;
import play.mvc.results.RenderBinary;
import properties.FapProperties;

import reports.Report;
import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import utils.ResolucionUtils.LineasResolucionSortComparator;
import utils.StringUtils;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;
import enumerado.fap.gen.EstadoTipoMultipleEnum;
import enumerado.fap.gen.EstadosDocBaremacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.ModalidadResolucionEnum;
import enumerado.fap.gen.TipoResolucionEnum;
import messages.Messages;
import models.Documento;
import models.Evaluacion;
import models.ExpedienteAed;
import models.LineaResolucionFAP;
import models.Registro;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import models.TableKeyValue;
import models.TipoEvaluacion;

@InjectSupport
public class ResolucionBase {

	@Inject
    public static GestorDocumentalService gestorDocumentalService;
	
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer.html";
	private final static String BODY_REPORT = "reports/resolucion/resolucion.html";
	private final static String BODY_REPORT_BAREMACION_CON_COMENTARIOS = "reports/baremacion/oficialEvaluacionCompleto.html";
	private final static String BODY_REPORT_BAREMACION_SIN_COMENTARIOS = "reports/baremacion/oficialEvaluacionCompleto.html";
	private final static String HEADER_BAREMACION_INDIVIDUAL_REPORT = "reports/header.html";
	private final static String BODY_BAREMACION_INDIVIDUAL_REPORT = "reports/resolucion/criteriosResolucion.html";
	private final static String FOOTER_BAREMACION_INDIVIDUAL_REPORT = "reports/footer-borrador.html";
	private final static String BODY_REPORT_OFICIO_REMISION = "reports/notificacion/notificacionBodyOficioRemision.html";
	private final static String TIPO_RESOLUCION_PROVISIONAL = FapProperties.get("fap.aed.tiposdocumentos.resolucion.provisional");
	private final static String TIPO_RESOLUCION_DEFINITIVA = FapProperties.get("fap.aed.tiposdocumentos.resolucion.definitiva");
	public ResolucionFAP resolucion;
	
	public ResolucionBase (ResolucionFAP resolucion) {
		this.resolucion = resolucion;
	}
	
	public static String getBodyReportOficioRemision() {
		return ResolucionBase.BODY_REPORT_OFICIO_REMISION;
	}
	
	public static String getHeaderReport() {
		return ResolucionBase.HEADER_REPORT;
	}
	
	public static String getFooterReport() {
		return ResolucionBase.FOOTER_REPORT;
	}
	
	public String getBodyReport() {
		return ResolucionBase.BODY_REPORT;
	}
	
	public String getBodyBaremacionConComentariosReport(){
		return ResolucionBase.BODY_REPORT_BAREMACION_CON_COMENTARIOS;
	}
	
	public String getBodyBaremacionSinComentariosReport(){
		return ResolucionBase.BODY_REPORT_BAREMACION_SIN_COMENTARIOS;
	}
	
	public static String getHeaderBaremacionIndividualReport() {
		return ResolucionBase.HEADER_BAREMACION_INDIVIDUAL_REPORT;
	}
	
	public String getBodyBaremacionIndividualReport() {
		return ResolucionBase.BODY_BAREMACION_INDIVIDUAL_REPORT;
	}
	
	public static String getFooterBaremacionIndividualReport() {
		return ResolucionBase.FOOTER_BAREMACION_INDIVIDUAL_REPORT;
	}
	
	
	public String getTipoRegistroResolucion(String tipo) {
		if (tipo != null) {
			if (TipoResolucionEnum.provisional.name().equals(tipo))
				return ResolucionBase.TIPO_RESOLUCION_PROVISIONAL;
		}
		return ResolucionBase.TIPO_RESOLUCION_DEFINITIVA;
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
	 *  Métodos que indican que documentos de Baremación deberán generarse para el tipo de resolucion
	 *  Deberán sobreescribirse en cada tipo de Resolucion
	 */
	
	public boolean isGenerarDocumentoBaremacionIndividual(){
		if (this.resolucion.conBaremacion)
			return true; // Por defecto se generaran siempre
		return false;
	}
	
	public boolean isGenerarDocumentoBaremacionCompletoSinComentarios(){
		return false;
	}
	
	public boolean isGenerarDocumentoBaremacionCompletoConComentarios(){
		return false;
	}
	
	/**
	 * Inicializamos la resolución, una vez que sabemos el tipo
	 */
	public void initResolucion(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		resolucion.firmarJefeServicio = true;
		resolucion.firmarDirector = true;
		resolucion.permitirPortafirma = true;
		resolucion.permitirRegistrar = true;
		resolucion.permitirPublicar = true;
		resolucion.estado = EstadoResolucionEnum.borrador.name();
		resolucion.save();
	}
	
	/**
	 * Devuelve las solicitudes "posibles" a resolver (lista desde donde se seleccionará)
	 * @return
	 */
	public java.util.List<?> getSolicitudesAResolver (Long idResolucion) {
		return SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.estado not in('borrador')").fetch();
	}
	
	/**
	 * Establece las líneas de resolución a una resolución, en caso de que no existan.
	 * @param idResolucion
	 */
	public void setLineasDeResolucion(Long idResolucion) {
	}
	
	/**
	 * Prepara todo antes de obtener las líneas de resolución.
	 * @param idResolucion
	 */
	public void prepararLineasResolucion (Long idResolucion) {
		
	}
	
	/**
	 * Validación al inicio de la resolución
	 * @param idResolLong
	 */
	public static void validaInicioResolucion (Long idResolucion) {
		
	}
	
	/**
	 * Comprueba que las líneas de resolución estén en un estado "válido"
	 * @param idResolucion
	 */
	public static void validarLineasResolucion (Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		if (resolucion.estado.equals(EstadoResolucionEnum.borrador.name())) {
			for (LineaResolucionFAP lResolucion : resolucion.lineasResolucion) {
				// TODO
			}
		}
	}
	
	/**
	 * Prepara la resolución para ser firmada
	 * 1. Valida
	 * 2. Elimina el borrador y oficial anterior
	 * 3. Genera y almacena los nuevos borrador y oficial
	 * 
	 * @param idResolucion
	 */
	public void prepararResolucion(Long idResolucion){
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
        
        validar();
        eliminarBorradorResolucion(resolucion);
        eliminarOficialResolucion(resolucion);
        File borrador = generarBorradorResolucion(resolucion);
        File oficial = generarOficialResolucion(resolucion);
        almacenarEnGestorDocumentalResolucion(resolucion, borrador, oficial);    
        avanzarFase_Creada(resolucion);
    }
	
	/**
	 * Permite modificar la resolución una vez ya preparada,
	 * por ejemplo tras ser rechazada.
	 *  
	 * @param idResolucion
	 */
	public void retrocederFase_Modificacion(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.creada.name();
			resolucion.save();
		}		
	}

	/**
	 * Sobreescribir si queremos realizar alguna validación en la resolución antes de preparar.
	 * 
	 */
	public void validar() {

	}
	
	 public void eliminarBorradorResolucion(ResolucionFAP resolucion){
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
	 
	 public void eliminarOficialResolucion(ResolucionFAP resolucion) {
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
	 
	 public File generarBorradorResolucion(ResolucionFAP resolucion) {
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
				resolucion.registro.borrador.tipo = getTipoRegistroResolucion(resolucion.tipo);
				resolucion.registro.save();
			} catch (Exception ex2) {
				Messages.error("Error generando el documento borrador "+ex2);
				play.Logger.error("Error generando el documento borrador: " + ex2.getMessage());
			}
		}

		return borrador;
	 }
	
	 public File generarOficialResolucion(ResolucionFAP resolucion){
		File oficial = null;
		if (!Messages.hasErrors()) {
			try {
				play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("resolucion", resolucion);
				oficial = new Report(this.getBodyReport()).header(this.getHeaderReport()).normalSize().renderTmpFile(resolucion);
				resolucion.registro.oficial = new Documento();
				resolucion.registro.oficial.tipo = getTipoRegistroResolucion(resolucion.tipo);
				resolucion.registro.save();
			} catch (Exception ex2) {
				Messages.error("Error generando el documento oficial");
				play.Logger.error("Error generando el documento oficial: " + ex2.getMessage());
			}
		}
		return oficial;
	 }
	 
	 public void almacenarEnGestorDocumentalResolucion(ResolucionFAP resolucion, File borrador, File oficial){
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
	 
	 public static List<String> getOrdenEstados () {
		 List<TableKeyValue> lista = TableKeyValue.findByTable("estadoLineaResolucion");
		 List<String> listRet = new ArrayList<String>();
		 for (TableKeyValue tk : lista) {
			 listRet.add(tk.key);
		 }
		 return listRet;
	 }
	 
	 public void publicarCambiarEstadoYDatos(long idResolucion){
		//ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
			ResolucionBase resolucion = null;
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}catch (Throwable e) {
				// TODO: handle exception
			}
			
			play.Logger.info("Resolución: "+resolucion.resolucion.id+" tiene "+resolucion.resolucion.lineasResolucion.size()+" líneas de resolución");

			for (LineaResolucionFAP linea: resolucion.resolucion.lineasResolucion) {
				play.Logger.info("Línea: "+linea.id+" estado: "+linea.estado);
				SolicitudGenerica sol = SolicitudGenerica.findById(linea.solicitud.id);

				//Cambio de estado de las solicitudes
				//Sacar un método único
//				if (TipoResolucionEnum.provisional.name().equals(resolucion.resolucion.tipo)) {
//					cambiaEstadoProvisional(linea);
//				} else {
//					cambiaEstadoDefinitiva(linea);
//				}
				sol.save();
			}
	 }
	
	 public void publicarCopiarEnExpedientes (long idResolucion){
		//ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
			ResolucionBase resolucion = null;
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}catch (Throwable e) {
				// TODO: handle exception
			}
			
			GestorDocumentalService gestorDocumental = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
			List<ExpedienteAed> listaExpedientes = new ArrayList<ExpedienteAed>();
			// ATENCIÓN:
			// 		LISTAEXPEDIENTES SE CREA PERO NO SE MODIFICA NUNCA
			//
			int i = 1;
			play.Logger.info("Resolución: "+resolucion.resolucion.id+" tiene "+resolucion.resolucion.lineasResolucion.size()+" líneas de resolución");

			for (LineaResolucionFAP linea: resolucion.resolucion.lineasResolucion) {
				SolicitudGenerica sol = SolicitudGenerica.findById(linea.solicitud.id);
				if ((i%10 == 0) || (i == resolucion.resolucion.lineasResolucion.size())) {
					try {
						gestorDocumental.copiarDocumentoEnExpediente(resolucion.resolucion.registro.oficial.uri, listaExpedientes);
						listaExpedientes.clear();
						play.Logger.info("Copiados los expedientes "+i);
					} catch (GestorDocumentalServiceException e) {
						play.Logger.error("No se han podido copiar el documento de resolución a los expedientes: "+i+" -> "+e);
						Messages.error("No se han podido copiar el documento de resolución a los expedientes");
					}
				}
				i++;

				sol.save();
			}

			// Si quedan expedientes por copiar:
			try {
				if (listaExpedientes.size() != 0) {
					gestorDocumental.copiarDocumentoEnExpediente(resolucion.resolucion.registro.oficial.uri, listaExpedientes);
					listaExpedientes.clear();
					play.Logger.info("Copiados los expedientes restantes");
				} else {
					play.Logger.info("No quedan expedientes a los que copiar la resolución");
				}
			} catch (GestorDocumentalServiceException e) {
				play.Logger.error("No se han podido copiar el documento de resolución a los expedientes: "+" -> "+e);
				Messages.error("No se han podido copiar el documento de resolución a los expedientes");
			}
			
			//Una vez copiados los expedientes se comprueba si hay documentos de baremacion
			//Si no hay, ha terminado la publicacion
			
			if (!resolucion.resolucion.conBaremacion) {
				EntityTransaction tx = JPA.em().getTransaction();
				tx.commit();
				tx.begin();
				if (EstadoResolucionEnum.notificada.equals(resolucion.resolucion.estado))
					resolucion.avanzarFase_Registrada_PublicadaYNotificada(resolucion.resolucion);
				else
					resolucion.avanzarFase_Registrada_Publicada(resolucion.resolucion);
				tx.commit();
				tx.begin();
		} 
	 }
	 
	 //TODO: Hacen falta dos metodos por culpa de la firma intermedia entre generacion y registro
	 public void notificarCopiarEnExpedientes (long idResolucion){
			ResolucionBase resolucion = null;
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}catch (Throwable e) {
				// TODO: handle exception
			}
			
			List<ExpedienteAed> listaExpedientes = new ArrayList<ExpedienteAed>();
			play.Logger.info("Resolución: "+resolucion.resolucion.id+" tiene "+resolucion.resolucion.lineasResolucion.size()+" líneas de resolución");
			GestorDocumentalService gestorDocumental = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
			FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);

			for (LineaResolucionFAP linea: resolucion.resolucion.lineasResolucion) {
				try  {
					//Genera el documento oficio de remision
					SolicitudGenerica sol = SolicitudGenerica.findById(linea.solicitud.id);
					File documentoOficioRemision = generarDocumentoOficioRemision(linea);
					String uri = gestorDocumental.saveDocumentoTemporal(linea.documentoOficioRemision, documentoOficioRemision);
					//Firmarlo
					firmaService.firmarEnServidor(linea.documentoOficioRemision);
					listaExpedientes.add(linea.solicitud.expedienteAed);
					//Y copiarlo al expediente
					gestorDocumental.copiarDocumentoEnExpediente(uri, listaExpedientes);
					listaExpedientes.clear();
					sol.save();
				} catch (Throwable e)   {
					
				}
			}
			
			//Una vez copiados los expedientes se comprueba si hay documentos de baremacion
			//y se avanza de fase segun el tipo de la resolucion
			if (!resolucion.resolucion.conBaremacion) {
					EntityTransaction tx = JPA.em().getTransaction();
					tx.commit();
					tx.begin();
					if (EstadoResolucionEnum.publicada.equals(resolucion.resolucion.estado))
						resolucion.avanzarFase_Registrada_PublicadaYNotificada(resolucion.resolucion);
					else
						resolucion.avanzarFase_Registrada_Notificada(resolucion.resolucion);
					tx.commit();
					tx.begin();
			} 
	 }
	 
	 public void publicarGenerarDocumentoBaremacionEnResolucion (long idResolucion){
		 
	 }
	 
	public void avanzarFase_Borrador(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.creada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Creada(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			if (resolucion.firmarJefeServicio) {
				resolucion.estado = EstadoResolucionEnum.preparada.name();
			}
			else if (!resolucion.firmarJefeServicio) {
				if (resolucion.firmarDirector) {
					resolucion.estado = EstadoResolucionEnum.firmadaJefeServicio.name();
				}
				else if (!resolucion.firmarDirector) {
					resolucion.estado = EstadoResolucionEnum.firmada.name();
				}
			}
			resolucion.save();
		}
	}
	
	public void avanzarFase_Preparada(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.pendienteFirmaJefeServicio.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Preparada_FirmaJefeServicio(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.firmadaJefeServicio.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Preparada_Portafirma(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.pendienteFirmaDirector.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_PendienteFirmarJefeServicio(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			if (resolucion.firmarDirector) {
				resolucion.estado = EstadoResolucionEnum.firmadaJefeServicio.name();
			}
			else {
				resolucion.estado = EstadoResolucionEnum.firmada.name();
			}
			resolucion.save();
		}
	}
	
	public void avanzarFase_FirmadaJefeServicio(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.pendienteFirmaDirector.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_PendienteFirmarDirector(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.firmada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Firmada(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.registrada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Registrada_Publicada(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.publicada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Registrada_Notificada(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.notificada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Registrada_PublicadaYNotificada(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.publicadaYNotificada.name();
			resolucion.save();
		}
	}
	
	public void avanzarFase_Publicada(ResolucionFAP resolucion) {
		if (!Messages.hasErrors()) {
			resolucion.estado = EstadoResolucionEnum.finalizada.name();
			resolucion.save();
		}
	}
	
	/**
	 * Devuelve las líneas de Resolución a las que se le añadirá el documento de baremación visible al usuario
	 * @param resolucion
	 * @return
	 */

	public void generarDocumentosBaremacionIndividualResolucion (Long idResolucion) {
		ResolucionFAP resolucionFap = ResolucionFAP.findById(idResolucion);
		ResolucionBase res = null;
		
		EntityTransaction tx = JPA.em().getTransaction();

		try {
			tx.commit();
			res = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			
			// Si tiene Baremación
			if (resolucionFap.conBaremacion) {
				List<LineaResolucionFAP> lineas = res.getLineasDocBaremacion(resolucionFap);
				if (lineas != null) {
					for (LineaResolucionFAP linea : lineas) {
						tx.begin();
						if (((linea.docBaremacion.uri == null) || (linea.docBaremacion.uri.isEmpty()))) {
							// 1. TODO: Generar documento en linea.docBaremacion
							File docBaremacionOficial = res.generarDocumentoBaremacion(linea);
							// 2. Subir al AED el File anterior
							gestorDocumentalService.saveDocumentoTemporal(linea.docBaremacion, docBaremacionOficial);
							play.Logger.info("Línea "+linea.id+": Guardado el documento de Evaluación "+linea.docBaremacion);
						}
						tx.commit();
					}
				}
			}
			tx.begin();
			res.resolucion.estadoDocBaremacionResolucion=EstadosDocBaremacionEnum.generado.name(); //Estado a docs Generados
			res.resolucion.save();
			tx.commit();
		} catch (Throwable e) {
		// TODO Auto-generated catch block
			play.Logger.error("Error al obtener el objeto de Resolución"+e);
			Messages.error("Error al generar los documentos de Resolución");
		}

	}
	
	public void clasificarDocumentosBaremacionIndividual(long idResolucion){
		ResolucionFAP resolucionFap = ResolucionFAP.findById(idResolucion);
		ResolucionBase res = null;
		
		EntityTransaction tx = JPA.em().getTransaction();
		tx.commit();
		try {
			res = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			
			// Si tiene Baremación
			if (resolucionFap.conBaremacion) {
				List<LineaResolucionFAP> lineas = res.getLineasDocBaremacion(resolucionFap);
				if (lineas != null) {
					for (LineaResolucionFAP linea : lineas) {
						tx.begin();
						// 3. Clasificar el documento en el Expediente de la Solicitud
						if (!linea.docBaremacion.clasificado) {
							List<Documento> listDocs = new ArrayList<Documento>();
							listDocs.add(linea.docBaremacion);
							gestorDocumentalService.clasificarDocumentos(linea.solicitud, listDocs);
							linea.docBaremacion.clasificado = true;
							linea.save();
							play.Logger.info("Línea "+linea.id+": Clasificado el documento de Evaluación "+linea.docBaremacion);
						}
						
						// 4. Hacerlo visible en la lista de documentos de la Solicitud
						boolean encontrado = false;
						for (Documento doc : linea.solicitud.documentacion.documentos) {
							if (linea.docBaremacion.uri.equals(doc.uri)) {
								encontrado = true;
								break;
							}
						}
						
						if (!encontrado) {
							linea.solicitud.documentacion.documentos.add(linea.docBaremacion);
							
						} else {
							play.Logger.info("El documento de resolución ya es visible en la solicitud");
						}
						linea.save();
						play.Logger.info("Documento de Resolución visible en la Solicitud "+linea.docBaremacion);
						tx.commit();
						play.Logger.info("Se clasificó correctamente el documento de evaluación para el expediente: "+linea.solicitud.expedienteAed.idAed);
					}
				} else {
					play.Logger.warn("No existen líneas a las que añadirle el documento de baremación");
					Messages.warning("No existen líneas a las que añadirle el documento de baremación");
				}
			}
			tx.begin();

			res.resolucion.estadoDocBaremacionResolucion=EstadosDocBaremacionEnum.clasificado.name(); //Estado a docs Generados
			res.resolucion.save();
			tx.commit();

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			play.Logger.error("Error al obtener el objeto de Resolución"+e);
			Messages.error("Error al generar los documentos de Resolución");
		}
	}
	
	public File generarDocumentoBaremacion (LineaResolucionFAP linea) {
		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", linea.solicitud);
		File report = null;
		try {
			report = new Report(getBodyBaremacionIndividualReport()).header(getHeaderBaremacionIndividualReport()).footer(getFooterBaremacionIndividualReport()).renderTmpFile(linea.solicitud);
			
			linea.docBaremacion = new Documento();
			linea.docBaremacion.tipo = getTipoDocumentoResolucionIndividual();
			linea.docBaremacion.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return report;
	}
	
	public File generarDocumentoOficialBaremacionConComentarios (LineaResolucionFAP linea) {
		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", linea.solicitud);
		File report = null;
		try {
			Evaluacion evaluacion = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.solicitud.id=?", linea.solicitud.id).first();
		 	String titulo = "Informe de Evaluación Completo";
		 	Boolean comentariosAdministracion = false;
		 	TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
			Long duracion = (long) (tipoEvaluacion.duracion-1);
		 	
		 	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("evaluacion", evaluacion);
		 	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("duracion", duracion);
		 	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("titulo", titulo);
		 	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("comentariosAdministracion", comentariosAdministracion);
		 	
		 	report = new Report(getBodyBaremacionConComentariosReport()).header(getHeaderBaremacionIndividualReport()).footer(getFooterBaremacionIndividualReport()).renderTmpFile(evaluacion, duracion, titulo, comentariosAdministracion);
			
			linea.docEvaluacionCompletoConComentarios = new Documento();
			linea.docEvaluacionCompletoConComentarios.tipo = getTipoDocumentoOficialConComentarios();
			linea.docEvaluacionCompletoConComentarios.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return report;
	}
	
	public File generarDocumentoOficialBaremacionSinComentarios (LineaResolucionFAP linea) {
		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", linea.solicitud);
		File report = null;
		Evaluacion evaluacion = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.solicitud.id=?", linea.solicitud.id).first();
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		Long duracion = (long) (tipoEvaluacion.duracion-1);
		
		try {
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("evaluacion", evaluacion);
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("duracion", duracion);
			report = new Report(getBodyBaremacionSinComentariosReport()).header(getHeaderBaremacionIndividualReport()).footer(getFooterBaremacionIndividualReport()).renderTmpFile(evaluacion, duracion);
			
			
			linea.docEvaluacionCompleto = new Documento();
			linea.docEvaluacionCompleto.tipo = getTipoDocumentoOficialSinComentarios();
			linea.docEvaluacionCompleto.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return report;
	}
	
	//TODO: Modificar para utilizar documentoOficioRemison de lineaResolucionFAP
	//      Y tener en cuenta la solicitud para generar el documento
	public File generarDocumentoOficioRemision (LineaResolucionFAP linea) {
		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("linea.solicitud", linea.solicitud);
		File report = null;
		try {
			report = new Report(getBodyReportOficioRemision()).header(getHeaderReport()).footer(getFooterReport()).renderTmpFile(linea.solicitud, this.resolucion);
			
			linea.documentoOficioRemision = new Documento();
			linea.documentoOficioRemision.tipo = getTipoDocumentoResolucionIndividual();
			linea.documentoOficioRemision.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return report;
	}
	
	public List<LineaResolucionFAP> getLineasDocBaremacion(ResolucionFAP resolucion){
		List<LineaResolucionFAP> lista = new ArrayList<LineaResolucionFAP>();
		for (LineaResolucionFAP linea : resolucion.lineasResolucion) {
			if (linea.estado.equals("concedida")) // Base
				lista.add(linea);
		}
		return lista;
	}

	public void saveDocumentoBaremacion (LineaResolucionFAP linea, File docBaremacionOficial) {
	
	}


	public void firmarDocumentosBaremacionEnResolucion (ResolucionBase resolucion){ 
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		if(resolucion.isGenerarDocumentoBaremacionCompletoConComentarios() && 
				resolucion.resolucion.estadoInformeBaremacionConComentarios != null 
				&& resolucion.resolucion.estadoInformeBaremacionConComentarios.equals(EstadosDocBaremacionEnum.clasificado.name())){
			//Firmar
			for (LineaResolucionFAP linea : resolucion.resolucion.lineasResolucion) {
				try {
					String firma = firmaService.firmarEnServidor(linea.docEvaluacionCompleto);
					play.Logger.info("La firma es: "+firma);
					linea.solicitud.save();
					Messages.ok("Se realizó la firma en Servidor correctamente");
				} catch (FirmaServiceException e) {
					// TODO Auto-generated catch block
					play.Logger.error("No se pudo firmar en Servidor: "+e);
				} 
			}
		}
		
		if(resolucion.isGenerarDocumentoBaremacionCompletoSinComentarios() && 
				resolucion.resolucion.estadoInformeBaremacionSinComentarios != null 
				&& resolucion.resolucion.estadoInformeBaremacionSinComentarios.equals(EstadosDocBaremacionEnum.clasificado.name())){
			//Firmar
			
			for (LineaResolucionFAP linea : resolucion.resolucion.lineasResolucion) {
				try {
					String firma = firmaService.firmarEnServidor(linea.docEvaluacionCompleto);
					play.Logger.info("La firma es: "+firma);
					linea.solicitud.save();
					Messages.ok("Se realizó la firma en Servidor correctamente");
				} catch (FirmaServiceException e) {
					// TODO Auto-generated catch block
					play.Logger.error("No se pudo firmar en Servidor: "+e);
				} 
			}
				
		}
		EntityTransaction tx = JPA.em().getTransaction();
		tx.commit();
		tx.begin();
		if (EstadoResolucionEnum.notificada.equals(resolucion.resolucion.estado))
			resolucion.avanzarFase_Registrada_PublicadaYNotificada(resolucion.resolucion);
		else
			resolucion.avanzarFase_Registrada_Publicada(resolucion.resolucion);
		tx.commit();
		tx.begin();

	}
		

	public String getTipoDocumentoResolucionIndividual(){
		return FapProperties.get("fap.aed.tiposdocumentos.evaluacion");

	}
	
	public String getTipoDocumentoOficialConComentarios(){
		return FapProperties.get("fap.aed.tiposdocumentos.evaluacion.completa.concomentarios");

	}
	
	public String getTipoDocumentoOficialSinComentarios(){
		return FapProperties.get("fap.aed.tiposdocumentos.evaluacion.completa");

	}
	
	public void generarDocumentoOficialBaremacionConComentarios(Long idResolucion){
			ResolucionFAP resolucionFap = ResolucionFAP.findById(idResolucion);
			ResolucionBase res = null;
			
			EntityTransaction tx = JPA.em().getTransaction();
			tx.commit();
			try {
				res = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
				
				// Si tiene Baremación
				if (resolucionFap.conBaremacion) {
					List<LineaResolucionFAP> lineas = res.getLineasDocBaremacion(resolucionFap);
					if (lineas != null) {
						for (LineaResolucionFAP linea : lineas) {
							tx.begin();
							
							if (((linea.docEvaluacionCompletoConComentarios.uri == null) || (linea.docEvaluacionCompletoConComentarios.uri.isEmpty()))) {
								// 1. TODO: Generar documento en linea.docBaremacion
								File docBaremacionOficial = res.generarDocumentoOficialBaremacionConComentarios(linea);
								// 2. Subir al AED el File anterior
								gestorDocumentalService.saveDocumentoTemporal(linea.docEvaluacionCompletoConComentarios, docBaremacionOficial);
							}
							tx.commit();
						}
					}
				}
				tx.begin();
				res.resolucion.estadoInformeBaremacionConComentarios=EstadosDocBaremacionEnum.generado.name(); //Estado a docs Generados
				res.resolucion.save();
				tx.commit();
			} catch (Throwable e) {
			// TODO Auto-generated catch block
				play.Logger.error("Error al obtener el objeto de Resolución"+e);
				Messages.error("Error al generar los documentos de Resolución");
			}
				
	}

	
	public void generarDocumentoOficialBaremacionSinComentarios(Long idResolucion){
			ResolucionFAP resolucionFap = ResolucionFAP.findById(idResolucion);
			ResolucionBase res = null;
			
			EntityTransaction tx = JPA.em().getTransaction();
			tx.commit(); 
			try {
				res = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
				
				// Si tiene Baremación
				if (resolucionFap.conBaremacion) {
					List<LineaResolucionFAP> lineas = res.getLineasDocBaremacion(resolucionFap);
					if (lineas != null) {
						for (LineaResolucionFAP linea : lineas) {
							tx.begin();
							
							if (((linea.docEvaluacionCompleto.uri == null) || (linea.docEvaluacionCompleto.uri.isEmpty()))) {
								// 1. TODO: Generar documento en linea.docBaremacion
								File docBaremacionOficial = res.generarDocumentoOficialBaremacionSinComentarios(linea);
								// 2. Subir al AED el File anterior
								gestorDocumentalService.saveDocumentoTemporal(linea.docEvaluacionCompleto, docBaremacionOficial);
							}
							tx.commit();
						}
					}
				}
				tx.begin();
				res.resolucion.estadoInformeBaremacionSinComentarios=EstadosDocBaremacionEnum.generado.name(); //Estado a docs Generados
				res.resolucion.save();
				tx.commit();
			} catch (Throwable e) {
			// TODO Auto-generated catch block
				play.Logger.error("Error al obtener el objeto de Resolución"+e);
				Messages.error("Error al generar los documentos de Resolución");
			}
				
	}
	
	public void clasificarDocumentoOficialBaremacionConComentarios(long idResolucion){
		ResolucionFAP resolucionFap = ResolucionFAP.findById(idResolucion);
		ResolucionBase res = null;
		
		EntityTransaction tx = JPA.em().getTransaction();
		tx.commit();
		try {
			res = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			
			// Si tiene Baremación
			if (resolucionFap.conBaremacion) {
				List<LineaResolucionFAP> lineas = res.getLineasDocBaremacion(resolucionFap);
				if (lineas != null) {
					for (LineaResolucionFAP linea : lineas) {
						tx.begin();
						// 3. Clasificar el documento en el Expediente de la Solicitud
						if (!linea.docEvaluacionCompletoConComentarios.clasificado) {
							List<Documento> listDocs = new ArrayList<Documento>();
							listDocs.add(linea.docBaremacion);
							gestorDocumentalService.clasificarDocumentos(linea.solicitud, listDocs);
							linea.docEvaluacionCompletoConComentarios.clasificado = true;
							linea.save();
						}
						tx.commit();
						play.Logger.info("Se clasificó correctamente el documento de evaluación para el expediente: "+linea.solicitud.expedienteAed.idAed);
					}
				}
			}
			tx.begin();
			res.resolucion.estadoInformeBaremacionConComentarios=EstadosDocBaremacionEnum.clasificado.name(); //Estado a docs Generados
			res.resolucion.save();
			tx.commit();
			
//			//Una vez clasificados todos los documentos, termina la publicacion
//			if (((res.isGenerarDocumentoBaremacionCompletoSinComentarios()) && ((resolucionFap.estadoInformeBaremacionSinComentarios != null) 
//					&& (EstadosDocBaremacionEnum.clasificado.name().equals(resolucionFap.estadoInformeBaremacionSinComentarios.toString()))))
//					||(!res.isGenerarDocumentoBaremacionCompletoSinComentarios())){
//				tx.begin();
//				res.avanzarFase_Registrada(resolucionFap);
//				tx.commit();
//			}
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			play.Logger.error("Error al obtener el objeto de Resolución"+e);
			Messages.error("Error al generar los documentos de Resolución");
		}
	}
	
	public void clasificarDocumentoOficialBaremacionSinComentarios(long idResolucion){
		ResolucionFAP resolucionFap = ResolucionFAP.findById(idResolucion);
		ResolucionBase res = null;
		
		EntityTransaction tx = JPA.em().getTransaction();
		tx.commit();
		try {
			res = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			
			// Si tiene Baremación
			if (resolucionFap.conBaremacion) {
				List<LineaResolucionFAP> lineas = res.getLineasDocBaremacion(resolucionFap);
				if (lineas != null) {
					for (LineaResolucionFAP linea : lineas) {
						tx.begin();
						// 3. Clasificar el documento en el Expediente de la Solicitud
						if (!linea.docEvaluacionCompleto.clasificado) {
							List<Documento> listDocs = new ArrayList<Documento>();
							listDocs.add(linea.docBaremacion);
							gestorDocumentalService.clasificarDocumentos(linea.solicitud, listDocs);
							linea.docEvaluacionCompleto.clasificado = true;
							linea.save();
						}
						tx.commit();
						play.Logger.info("Se clasificó correctamente el documento de evaluación para el expediente: "+linea.solicitud.expedienteAed.idAed);
					}
				}
			}
			tx.begin();
			res.resolucion.estadoInformeBaremacionSinComentarios=EstadosDocBaremacionEnum.clasificado.name(); //Estado a docs Generados
			res.resolucion.save();
			tx.commit();
			
//			//Una vez clasificados todos los documentos, termina la publicacion
//			if (((res.isGenerarDocumentoBaremacionCompletoConComentarios()) 
//					&& ((resolucionFap.estadoInformeBaremacionConComentarios != null)
//					&& (EstadosDocBaremacionEnum.clasificado.name().equals(resolucionFap.estadoInformeBaremacionConComentarios.toString()))))
//				||(!res.isGenerarDocumentoBaremacionCompletoConComentarios())){
//				tx.begin();
//				res.avanzarFase_Registrada(resolucionFap);
//				tx.commit();
//			}
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			play.Logger.error("Error al obtener el objeto de Resolución"+e);
			Messages.error("Error al generar los documentos de Resolución");
		}
	}
	
	public static boolean isGeneradoDocumentoResolucion() {
		if (properties.FapProperties.getBoolean("fap.resoluciones.generarDocumentoResolucion"))
			return true;
		return false;
	}

	public static boolean isPublicarTablonAnuncios() {
		if (properties.FapProperties.getBoolean("fap.resoluciones.publicarTablonAnuncios"))
			return true;
		return false;
	}

	public static boolean isNotificar() {
		if (properties.FapProperties.getBoolean("fap.resoluciones.notificar"))
			return true;
		return false;
	}
	
}
