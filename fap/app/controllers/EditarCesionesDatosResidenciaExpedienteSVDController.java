package controllers;

import play.mvc.Util;
import services.verificacionDatos.SVDUtils;
import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import controllers.fap.AgenteController;
import controllers.gen.EditarCesionesDatosResidenciaExpedienteSVDControllerGen;

public class EditarCesionesDatosResidenciaExpedienteSVDController extends EditarCesionesDatosResidenciaExpedienteSVDControllerGen {

	public static void index(String accion, Long idSolicitudTransmisionSVDFAP, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/EditarCesionesDatosResidenciaExpedienteSVD/EditarCesionesDatosResidenciaExpedienteSVD.html");
		}

		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarCesionesDatosResidenciaExpedienteSVDController.getSolicitudTransmisionSVDFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitudTransmisionSVDFAP.save();
				idSolicitudTransmisionSVDFAP = solicitudTransmisionSVDFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarCesionesDatosResidenciaExpedienteSVDController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);
			solicitud = SVDUtils.getSolicitud(idSolicitud);
		}
		
		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/EditarCesionesDatosResidenciaExpedienteSVD/EditarCesionesDatosResidenciaExpedienteSVD.html" + " Agente: " + logAgente);
		renderTemplate("gen/EditarCesionesDatosResidenciaExpedienteSVD/EditarCesionesDatosResidenciaExpedienteSVD.html", accion, idSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP, idSolicitud, solicitud);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitudTransmisionSVDFAP, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarCesionesDatosResidenciaExpedienteSVDController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		
		EditarCesionesDatosResidenciaExpedienteSVDController.EditarCesionesDatosResidenciaExpedienteSVDBindReferences(solicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {
			EditarCesionesDatosResidenciaExpedienteSVDController.EditarCesionesDatosResidenciaExpedienteSVDValidateCopy("editar", dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}

		if (!Messages.hasErrors()) {
			EditarCesionesDatosResidenciaExpedienteSVDController.editarValidateRules(dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbSolicitudTransmisionSVDFAP.save();
			log.info("Acción Editar de página: " + "gen/EditarCesionesDatosResidenciaExpedienteSVD/EditarCesionesDatosResidenciaExpedienteSVD.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/EditarCesionesDatosResidenciaExpedienteSVD/EditarCesionesDatosResidenciaExpedienteSVD.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		EditarCesionesDatosResidenciaExpedienteSVDController.editarRender(idSolicitudTransmisionSVDFAP, dbSolicitudTransmisionSVDFAP.solicitud.getId());
	}
	
	@Util
	public static void editarRender(Long idSolicitudTransmisionSVDFAP, Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("EditarCesionesDatosResidenciaExpedienteSVDController.index", "editar", idSolicitudTransmisionSVDFAP, idSolicitud);
		}
		Messages.keep();
		redirect("EditarCesionesDatosResidenciaExpedienteSVDController.index", "editar", idSolicitudTransmisionSVDFAP, idSolicitud);
	}
}
