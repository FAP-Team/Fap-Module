package controllers;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import controllers.gen.DocumentosFAPControllerGen;

public class DocumentosFAPController extends DocumentosFAPControllerGen {
	public static void index(String accion, Long idSolicitud, Long idDocumento) {
        if (accion == null)
            accion = getAccion();
        if (!permiso(accion)) {
            Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
            renderTemplate("fap/Documentacion/DocumentosFAP.html");
        }

        SolicitudGenerica solicitud = DocumentosFAPController.getSolicitudGenerica(idSolicitud);

        Documento documento = null;
        if ("crear".equals(accion))
            documento = DocumentosFAPController.getDocumento();
        else if (!"borrado".equals(accion))
            documento = DocumentosFAPController.getDocumento(idSolicitud, idDocumento);

        log.info("Visitando página: " + "fap/Documentacion/DocumentosFAP.html");
        renderTemplate("fap/Documentacion/DocumentosFAP.html", accion, idSolicitud, idDocumento, solicitud, documento);
    }
}
