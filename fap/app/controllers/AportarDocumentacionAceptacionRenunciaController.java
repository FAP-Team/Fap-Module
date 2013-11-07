package controllers;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import controllers.fap.AgenteController;
import controllers.gen.AportarDocumentacionAceptacionRenunciaControllerGen;

public class AportarDocumentacionAceptacionRenunciaController extends AportarDocumentacionAceptacionRenunciaControllerGen {
	public static void index(String accion, Long idSolicitud, Long idDocumento) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/AportarDocumentacionAceptacionRenuncia/AportarDocumentacionAceptacionRenuncia.html");
		}

		SolicitudGenerica solicitud = AportarDocumentacionAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);

		Documento documento = null;
		if ("crear".equals(accion)) {
			documento = AportarDocumentacionAceptacionRenunciaController.getDocumento();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				documento.save();
				idDocumento = documento.id;
				solicitud.aceptarRenunciar.documentos.add(documento);
				solicitud.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			documento = AportarDocumentacionAceptacionRenunciaController.getDocumento(idSolicitud, idDocumento);

		log.info("Visitando página: " + "fap/AportarDocumentacionAceptacionRenuncia/AportarDocumentacionAceptacionRenuncia.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("fap/AportarDocumentacionAceptacionRenuncia/AportarDocumentacionAceptacionRenuncia.html", accion, idSolicitud, idDocumento, solicitud, documento);
	}
}
