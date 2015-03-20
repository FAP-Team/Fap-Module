package controllers;

import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import play.mvc.Util;
import utils.SVDUtils;
import controllers.fap.AgenteController;
import controllers.gen.EditarSolicitudTransmisionSVDIdentidadControllerGen;

public class EditarSolicitudTransmisionSVDIdentidadController extends EditarSolicitudTransmisionSVDIdentidadControllerGen {

	//Se pasa tambien el id de la Solicitud Generica
	public static void index(String accion, Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {

		SolicitudGenerica solicitud = SVDUtils.getSolicitud(idSolicitud);

		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html");
		}
		checkRedirigir();

		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		if ("crear".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP();

			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitudTransmisionSVDFAP.save();
				idSolicitudTransmisionSVDFAP = solicitudTransmisionSVDFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " Agente: " + logAgente);

		//Se pasa por el render la Solicitud Generica y la Solicitud de Transmision
		renderTemplate("fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html", accion, solicitud, solicitudTransmisionSVDFAP);
	}


	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitud, Long idSolicitudTransmisionSVDFAP, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		EditarSolicitudTransmisionSVDIdentidadController.EditarSolicitudTransmisionSVDIdentidadBindReferences(solicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {

			EditarSolicitudTransmisionSVDIdentidadController.EditarSolicitudTransmisionSVDIdentidadValidateCopy("editar", dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);

		}

		if (!Messages.hasErrors()) {
			EditarSolicitudTransmisionSVDIdentidadController.editarValidateRules(dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbSolicitudTransmisionSVDFAP.save();
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		EditarSolicitudTransmisionSVDIdentidadController.editarRender(idSolicitud, idSolicitudTransmisionSVDFAP);
	}


	public static void crear(Long idSolicitud, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		SolicitudGenerica solicitud = SVDUtils.getSolicitud(idSolicitud);
		solicitudTransmisionSVDFAP.solicitud = solicitud;

		SVDUtils.crearLogica("identidad", idSolicitud, solicitudTransmisionSVDFAP);
		EditarSolicitudTransmisionSVDIdentidadController.crearRender(idSolicitud, solicitudTransmisionSVDFAP.getId());
	}


	//Se modifica el render para que pase el id de la Solicitud Generica
	@Util
	public static void editarRender(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("EditarSolicitudTransmisionSVDIdentidadController.index", "editar", idSolicitud, idSolicitudTransmisionSVDFAP);
		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDIdentidadController.index", "editar", idSolicitud, idSolicitudTransmisionSVDFAP);
	}

	@Util
	public static void crearRender(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		if (!Messages.hasMessages()) {

			Messages.ok("Página creada correctamente");
			Messages.keep();
			redirect("CesionDatosExpedienteSVDController.index", "editar", idSolicitud);

		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDIdentidadController.index", "crear", idSolicitud, idSolicitudTransmisionSVDFAP);
	}

}
