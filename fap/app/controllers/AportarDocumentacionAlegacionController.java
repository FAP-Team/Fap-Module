package controllers;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import controllers.fap.AgenteController;
import controllers.gen.AportarDocumentacionAlegacionControllerGen;

public class AportarDocumentacionAlegacionController extends AportarDocumentacionAlegacionControllerGen {
	public static void index(String accion, Long idSolicitud, Long idDocumento) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/AportarDocumentacionAlegacion/AportarDocumentacionAlegacion.html");
		}

		SolicitudGenerica solicitud = AportarDocumentacionAlegacionController.getSolicitudGenerica(idSolicitud);

		Documento documento = null;
		if ("crear".equals(accion)) {
			documento = AportarDocumentacionAlegacionController.getDocumento();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				documento.save();
				idDocumento = documento.id;
				solicitud.alegaciones.actual.documentos.add(documento);
				solicitud.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			documento = AportarDocumentacionAlegacionController.getDocumento(idSolicitud, idDocumento);

		log.info("Visitando página: " + "fap/AportarDocumentacionAlegacion/AportarDocumentacionAlegacion.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("fap/AportarDocumentacionAlegacion/AportarDocumentacionAlegacion.html", accion, idSolicitud, idDocumento, solicitud, documento);
	}
}
