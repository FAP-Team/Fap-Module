package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import play.mvc.Util;
import reports.Report;
import controllers.gen.DocumentacionFAPControllerGen;

public class DocumentacionFAPController extends DocumentacionFAPControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/DocumentacionFAP/DocumentacionFAP.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = DocumentacionFAPController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = DocumentacionFAPController.getSolicitudGenerica(idSolicitud);

		log.info("Visitando página: " + "fap/DocumentacionFAP/DocumentacionFAP.html");
		renderTemplate("gen/DocumentacionFAP/DocumentacionFAP.html", accion, idSolicitud, solicitud);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formAbrirPlantillaFH(Long idSolicitud, String btnAbrirPlantillaFH) {
		checkAuthenticity();
		if (!permisoFormAbrirPlantillaFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			try {
				SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
				new Report("reports/permitirFirmaFH.html").header("reports/header.html").renderResponse(solicitud);
			} catch (Exception e) {
				play.Logger.error("Error generando la plantilla de habilitar funcionario a firmar", e.getMessage());
				Messages.error("Error generando la plantilla de habilitar funcionario a firmar");
			}
		}

		if (!Messages.hasErrors()) {
			DocumentacionFAPController.formAbrirPlantillaFHValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/DocumentacionFAP/DocumentacionFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/DocumentacionFAP/DocumentacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		DocumentacionFAPController.formAbrirPlantillaFHRender(idSolicitud);
	}
}
