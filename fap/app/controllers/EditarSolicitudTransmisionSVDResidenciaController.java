package controllers;

import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import play.mvc.Util;
import utils.SVDUtils;
import controllers.fap.AgenteController;
import controllers.gen.EditarSolicitudTransmisionSVDResidenciaControllerGen;

public class EditarSolicitudTransmisionSVDResidenciaController extends EditarSolicitudTransmisionSVDResidenciaControllerGen {

	public static void index(String accion, Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {

		SolicitudGenerica solicitud = SVDUtils.getSolicitud(idSolicitud);

		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EditarSolicitudTransmisionSVDResidencia/EditarSolicitudTransmisionSVDResidencia.html");
		}
		checkRedirigir();

		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		if ("crear".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDResidenciaController.getSolicitudTransmisionSVDFAP();

			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitudTransmisionSVDFAP.save();
				idSolicitudTransmisionSVDFAP = solicitudTransmisionSVDFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDResidenciaController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/EditarSolicitudTransmisionSVDResidencia/EditarSolicitudTransmisionSVDResidencia.html" + " Agente: " + logAgente);
		renderTemplate("fap/EditarSolicitudTransmisionSVDResidencia/EditarSolicitudTransmisionSVDResidencia.html", accion, solicitud, solicitudTransmisionSVDFAP);
	}


	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitud, Long idSolicitudTransmisionSVDFAP, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDResidenciaController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		EditarSolicitudTransmisionSVDResidenciaController.EditarSolicitudTransmisionSVDResidenciaBindReferences(solicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {

			EditarSolicitudTransmisionSVDResidenciaController.EditarSolicitudTransmisionSVDResidenciaValidateCopy("editar", dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);

		}

		if (!Messages.hasErrors()) {
			EditarSolicitudTransmisionSVDResidenciaController.editarValidateRules(dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbSolicitudTransmisionSVDFAP.save();
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVDResidencia/EditarSolicitudTransmisionSVDResidencia.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVDResidencia/EditarSolicitudTransmisionSVDResidencia.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		EditarSolicitudTransmisionSVDResidenciaController.editarRender(idSolicitud, idSolicitudTransmisionSVDFAP);
	}

	public static void crear(Long idSolicitud, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		SolicitudGenerica solicitud = SVDUtils.getSolicitud(idSolicitud);
		solicitudTransmisionSVDFAP.solicitud = solicitud;

		SVDUtils.crearLogica("residencia", idSolicitud, solicitudTransmisionSVDFAP);
		EditarSolicitudTransmisionSVDResidenciaController.crearRender(idSolicitud, solicitudTransmisionSVDFAP.getId());
	}


	@Util
	public static void editarRender(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("CesionDatosExpedienteSVDController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDResidenciaController.index", "editar", idSolicitud, idSolicitudTransmisionSVDFAP);
	}

	@Util
	public static void crearRender(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		if (!Messages.hasMessages()) {

			Messages.ok("Página creada correctamente");
			Messages.keep();
			redirect("CesionDatosExpedienteSVDController.index", "editar", idSolicitud);

		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDResidenciaController.index", "crear", idSolicitud, idSolicitudTransmisionSVDFAP);
	}

}
