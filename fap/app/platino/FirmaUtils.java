package platino;

import java.io.File;
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
import services.AedService;
import services.FirmaService;
import services.RegistroService;

@InjectSupport
public class FirmaUtils {
	
	@Inject
	static FirmaService firmaService;
	
	@Inject
	static RegistroService registroService;
	
	@Inject
	static AedService aedService;
	
	/**
	 * Firma el documento
	 * @param documento
	 * @param listaFirmantes Lista de firmantes (debe haber sido calculada previamente)
	 * @param firma
	 * @param valorDocumentofirmanteSolicitado
	 */
	public static void firmar(Documento documento, List<Firmante> listaFirmantes, platino.Firma firma, String valorDocumentofirmanteSolicitado){
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
			Messages.ok("La solicitud se firmó correctamente");
			
			if(firmaService.hanFirmadoTodos(listaFirmantes)){
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
		aedService = InjectorConfig.getInjector().getInstance(AedService.class);
		if ((create) || (solicitud.registro.oficial == null) || (solicitud.registro.oficial.uri == null)) {
			try {
				//Genera el documento oficial
				File oficial =  new Report("reports/solicitud_simple.html").header("reports/header.html").registroSize().renderTmpFile(solicitud);
				solicitud.registro.oficial = new Documento();
				solicitud.registro.oficial.tipo = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
				solicitud.registro.oficial.descripcion = "Descripción del documento";
				solicitud.registro.fasesRegistro.borrador = true;
				aedService.saveDocumentoTemporal(solicitud.registro.oficial, oficial);
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
}
