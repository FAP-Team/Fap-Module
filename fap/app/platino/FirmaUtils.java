
package platino;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import config.InjectorConfig;

import messages.Messages;
import models.Documento;
import models.Firmante;
import models.Registro;
import models.RepresentantePersonaJuridica;
import models.Solicitante;
import models.SolicitudGenerica;
import play.modules.guice.InjectSupport;
import play.mvc.Controller;
import properties.FapProperties;
import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;
import services.RegistroService;

@InjectSupport
public class FirmaUtils {
	
	@Inject
	static FirmaService firmaService;
	
	@Inject
	static RegistroService registroService;
	
	@Inject
	static GestorDocumentalService gestorDocumentalService;
	
	/**
	 * Firma el documento
	 * @param documento
	 * @param listaFirmantes Lista de firmantes (debe haber sido calculada previamente)
	 * @param firma
	 * @param valorDocumentofirmanteSolicitado
	 */
	public static void firmar(Documento documento, List<Firmante> listaFirmantes, String firma, String valorDocumentofirmanteSolicitado){
		firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		
		if (documento == null) {
			Messages.error("No existe ningún documento para firmar");
			Messages.keep();
			return;			
		}
		if (firma == null) {
			Messages.error("La firma es null");
			return;
		}
		
		firmaService.firmar(documento, listaFirmantes, firma, valorDocumentofirmanteSolicitado);
		
		if (!Messages.hasMessages()) {
			Messages.ok("El documento se firmó correctamente");
			
			if(hanFirmadoTodos(listaFirmantes)){
				Messages.ok("La solicitud está preparada para el registro");
			}
		}
		Messages.keep();	
	}
	
	/**
	 * Genera el documento oficial de la solicitud, que se encuentra en "solicitud.registro.oficial", y cambia la fase de registro borrador = true
	 * @param solicitud
	 * @param create Si es true, lo vuelve a crear
	 * @return
	 */
	public static void generarOficial (SolicitudGenerica solicitud, boolean create) {
		gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		if ((create) || (solicitud.registro.oficial == null) || (solicitud.registro.oficial.uri == null)) {
			try {
				//Genera el documento oficial
				File oficial =  new Report("reports/solicitud_simple.html").header("reports/header.html").registroSize().renderTmpFile(solicitud);
				solicitud.registro.oficial = new Documento();
				solicitud.registro.oficial.tipo = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
				solicitud.registro.oficial.descripcion = "Descripción del documento";
				solicitud.registro.fasesRegistro.borrador = true;
				gestorDocumentalService.saveDocumentoTemporal(solicitud.registro.oficial, oficial);
				solicitud.save();
				play.Logger.info("Documento creado y subido al AED: ");
			} catch (Exception e) {
				Messages.error("Error al generar el documento oficial");
				e.printStackTrace();
			}
		} else {
			play.Logger.info("Documento YA existe al AED: ");
		}
		play.Logger.info("uri -> "+solicitud.registro.oficial.uri);
		play.Logger.info("url -> "+solicitud.registro.oficial.urlDescarga);
	}
	
	public static void generarOficial (SolicitudGenerica solicitud) {
		generarOficial(solicitud, false);
	}
	
	
	public static void calcularFirmantes(Solicitante solicitante, List<Firmante> firmantes){
		if(solicitante == null) throw new NullPointerException();
		if(firmantes == null) throw new NullPointerException();
		
		//Solicitante de la solicitud
		Firmante firmanteSolicitante = new Firmante(solicitante, "unico");
		firmantes.add(firmanteSolicitante);
		
		//Comprueba los representantes
		if(solicitante.isPersonaFisica() && solicitante.representado){
			// Representante de persona física
			Firmante representante = new Firmante(solicitante.representante, "representante", "unico");
			firmantes.add(representante);
		}else if(solicitante.isPersonaJuridica()){
			//Representantes de la persona jurídica
			for(RepresentantePersonaJuridica r : solicitante.representantes){
				String cardinalidad = null;
				if(r.tipoRepresentacion.equals("mancomunado")){
					cardinalidad = "multiple";
				}else if((r.tipoRepresentacion.equals("solidario")) || (r.tipoRepresentacion.equals("administradorUnico"))){
					cardinalidad = "unico";
				}
				Firmante firmante = new Firmante(r, "representante", cardinalidad);
				firmantes.add(firmante);
			}
		}
	}
	
	/**
	 * Borra una lista de firmantes, borrando cada uno de los firmantes y vaciando la lista
	 * @param firmantes
	 */
	public static void borrarFirmantes(List<Firmante> firmantes){
		List<Firmante> firmantesBack = new ArrayList<Firmante>(firmantes);
		firmantes.clear();
		
		for(Firmante f : firmantesBack)
			f.delete();
	}
	
	public static boolean hanFirmadoTodos(List<Firmante> firmantes){
		boolean multiple = true;
		for(Firmante f : firmantes){
			//Firmante único que ya ha firmado
			if(f.cardinalidad.equals("unico") && f.fechaFirma != null)
				return true;
			
			//Uno de los firmantes multiples no ha firmado
			if(f.cardinalidad.equals("multiple") && f.fechaFirma == null)
				multiple = false;
		}
		
		//En el caso de que no haya firmado ningún único
		//Se devuelve true si todos los múltiples han firmado
		return multiple;
	}
	
	
}
