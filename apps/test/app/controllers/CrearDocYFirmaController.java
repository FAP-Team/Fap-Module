package controllers;

import java.io.File;

import javax.inject.Inject;

import messages.Messages;
import models.Documento;
import models.Solicitud;
import play.modules.guice.InjectSupport;
import play.mvc.Util;
import properties.FapProperties;
import reports.Report;
import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import config.InjectorConfig;
import controllers.gen.CrearDocYFirmaControllerGen;

@InjectSupport
public class CrearDocYFirmaController extends CrearDocYFirmaControllerGen {

    @Inject
    public static GestorDocumentalService gestorDocumentalService;
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void generarDocumento(Long idSolicitud, String pdfDocumento) {
		checkAuthenticity();
		if (!permisoGenerarDocumento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		Solicitud solicitud = CrearDocYFirmaController.getSolicitud(idSolicitud);

		File fileOficial = null;
		if (!Messages.hasErrors()) {
			try {
				fileOficial = new Report("reports/solicitud_simple.html").header("reports/header.html").footer("reports/footer-borrador.html").registroSize().renderTmpFile(solicitud);
				solicitud.firmaEnServidor.oficial = new Documento();
				solicitud.firmaEnServidor.oficial.tipo = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
				solicitud.firmaEnServidor.oficial.descripcion = "Ejemplo de Generación Simple";
				gestorDocumentalService.saveDocumentoTemporal(solicitud.firmaEnServidor.oficial, fileOficial);
				solicitud.firmaEnServidor.fueGenerado = true;
				solicitud.save();
			}
			catch (GestorDocumentalServiceException ex) {
				play.Logger.error("Error guardando el documento "+ex.getMessage());
				Messages.error("Error guardando el documento "+ex.getMessage());
			}
			catch (Exception ex1) {
				play.Logger.error("Error en la generación del pdf "+ex1.getMessage());
				Messages.error("Error en la generación del pdf "+ex1.getMessage());
			}
		}

		if (!Messages.hasErrors()) {
			CrearDocYFirmaController.generarDocumentoValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/CrearDocYFirma/CrearDocYFirma.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/CrearDocYFirma/CrearDocYFirma.html" + " , intentada sin éxito (Problemas de Validación)");
		CrearDocYFirmaController.generarDocumentoRender(idSolicitud);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void firmarDocumento(Long idSolicitud, String firmaServidorDocumento) {
		checkAuthenticity();
		if (!permisoFirmarDocumento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		Solicitud solicitud = CrearDocYFirmaController.getSolicitud(idSolicitud);
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);


		if (!Messages.hasErrors()) {
			try {
				String firma = firmaService.firmarEnServidor(solicitud.firmaEnServidor.oficial);
				play.Logger.info("La firma es: "+firma);
				solicitud.firmaEnServidor.fueFirmado = true;
				solicitud.save();
				Messages.ok("Se realizó la firma en Servidor correctamente");
			} catch (FirmaServiceException e) {
				// TODO Auto-generated catch block
				play.Logger.error("No se pudo firmar en Servidor: "+e);
			} 
			

		}

		if (!Messages.hasErrors()) {
			CrearDocYFirmaController.firmarDocumentoValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/CrearDocYFirma/CrearDocYFirma.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/CrearDocYFirma/CrearDocYFirma.html" + " , intentada sin éxito (Problemas de Validación)");
		CrearDocYFirmaController.firmarDocumentoRender(idSolicitud);
	}
	
}
