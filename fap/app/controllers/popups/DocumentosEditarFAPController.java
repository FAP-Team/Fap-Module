package controllers.popups;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import controllers.gen.popups.DocumentosEditarFAPControllerGen;

public class DocumentosEditarFAPController extends DocumentosEditarFAPControllerGen {
	public static void index(String accion, Long idSolicitud, Long idDocumento) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/Documentacion/DocumentosEditarFAP.html");
		}

		SolicitudGenerica solicitud = DocumentosEditarFAPController.getSolicitudGenerica(idSolicitud);

		Documento documento = null;
		if ("crear".equals(accion))
			documento = DocumentosEditarFAPController.getDocumento();
		else if (!"borrado".equals(accion))
			documento = DocumentosEditarFAPController.getDocumento(idSolicitud, idDocumento);

		log.info("Visitando página: " + "fap/Documentacion/DocumentosEditarFAP.html");
		renderTemplate("fap/Documentacion/DocumentosEditarFAP.html", accion, idSolicitud, idDocumento, solicitud, documento);
	}
}
