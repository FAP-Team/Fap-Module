package controllers;

import play.mvc.Util;
import services.verificacionDatos.SVDServiceException;
import services.verificacionDatos.SVDUtils;
import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import controllers.fap.AgenteController;
import controllers.gen.EditarCesionesDatosIdentidadExpedienteSVDControllerGen;
import enumerado.fap.gen.NombreServicioSVDFAPEnum;

public class EditarCesionesDatosIdentidadExpedienteSVDController extends EditarCesionesDatosIdentidadExpedienteSVDControllerGen {

	public static void index(String accion, Long idSolicitudTransmisionSVDFAP, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/EditarCesionesDatosIdentidadExpedienteSVD/EditarCesionesDatosIdentidadExpedienteSVD.html");
		}

		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarCesionesDatosIdentidadExpedienteSVDController.getSolicitudTransmisionSVDFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitudTransmisionSVDFAP.save();
				idSolicitudTransmisionSVDFAP = solicitudTransmisionSVDFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarCesionesDatosIdentidadExpedienteSVDController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);
			solicitud = SVDUtils.getSolicitud(idSolicitud);
		}
		
		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/EditarCesionesDatosIdentidadExpedienteSVD/EditarCesionesDatosIdentidadExpedienteSVD.html" + " Agente: " + logAgente);
		renderTemplate("gen/EditarCesionesDatosIdentidadExpedienteSVD/EditarCesionesDatosIdentidadExpedienteSVD.html", accion, idSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP, idSolicitud, solicitud);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitudTransmisionSVDFAP, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarCesionesDatosIdentidadExpedienteSVDController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		EditarCesionesDatosIdentidadExpedienteSVDController.EditarCesionesDatosIdentidadExpedienteSVDBindReferences(solicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {
			EditarCesionesDatosIdentidadExpedienteSVDController.EditarCesionesDatosIdentidadExpedienteSVDValidateCopy("editar", dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}

		if (!Messages.hasErrors()) {
			EditarCesionesDatosIdentidadExpedienteSVDController.editarValidateRules(dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbSolicitudTransmisionSVDFAP.save();
			log.info("Acción Editar de página: " + "gen/EditarCesionesDatosIdentidadExpedienteSVD/EditarCesionesDatosIdentidadExpedienteSVD.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/EditarCesionesDatosIdentidadExpedienteSVD/EditarCesionesDatosIdentidadExpedienteSVD.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		EditarCesionesDatosIdentidadExpedienteSVDController.editarRender(idSolicitudTransmisionSVDFAP, dbSolicitudTransmisionSVDFAP.solicitud.getId());
	}
	
	@Util
	public static void editarRender(Long idSolicitudTransmisionSVDFAP, Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("EditarCesionesDatosIdentidadExpedienteSVDController.index", "editar", idSolicitudTransmisionSVDFAP, idSolicitud);
		}
		Messages.keep();
		redirect("EditarCesionesDatosIdentidadExpedienteSVDController.index", "editar", idSolicitudTransmisionSVDFAP, idSolicitud);
	}

}
