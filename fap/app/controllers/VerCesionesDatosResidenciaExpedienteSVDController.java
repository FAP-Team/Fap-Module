package controllers;

import services.verificacionDatos.SVDUtils;
import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import controllers.fap.AgenteController;
import controllers.gen.VerCesionesDatosResidenciaExpedienteSVDControllerGen;

public class VerCesionesDatosResidenciaExpedienteSVDController extends VerCesionesDatosResidenciaExpedienteSVDControllerGen {
	
	public static void index(String accion, Long idSolicitudTransmisionSVDFAP, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/VerCesionesDatosResidenciaExpedienteSVD/VerCesionesDatosResidenciaExpedienteSVD.html");
		}

		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitudTransmisionSVDFAP = VerCesionesDatosResidenciaExpedienteSVDController.getSolicitudTransmisionSVDFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitudTransmisionSVDFAP.save();
				idSolicitudTransmisionSVDFAP = solicitudTransmisionSVDFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion)) {
			solicitudTransmisionSVDFAP = VerCesionesDatosResidenciaExpedienteSVDController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);
			solicitud = SVDUtils.getSolicitud(idSolicitud);
		}
		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/VerCesionesDatosResidenciaExpedienteSVD/VerCesionesDatosResidenciaExpedienteSVD.html" + " Agente: " + logAgente);
		renderTemplate("gen/VerCesionesDatosResidenciaExpedienteSVD/VerCesionesDatosResidenciaExpedienteSVD.html", accion, idSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP, idSolicitud, solicitud);
	}


}
