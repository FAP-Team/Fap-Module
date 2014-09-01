
package controllers;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.SolicitudGenerica;
import controllers.fap.AgenteController;
import controllers.gen.DocumentosAportacionControllerGen;
			
public class DocumentosAportacionController extends DocumentosAportacionControllerGen {
	public static void index(String accion, Long idSolicitud, Long idDocumento) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/DocumentosAportacion/DocumentosAportacion.html");
		}

		SolicitudGenerica solicitud = DocumentosAportacionController.getSolicitudGenerica(idSolicitud);

		Documento documento = null;
		if ("crear".equals(accion)) {
			documento = DocumentosAportacionController.getDocumento();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				documento.save();
				idDocumento = documento.id;
				solicitud.aportaciones.actual.documentos.add(documento);
				solicitud.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			documento = DocumentosAportacionController.getDocumento(idSolicitud, idDocumento);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/DocumentosAportacion/DocumentosAportacion.html" + " Agente: " + logAgente);
		renderTemplate("fap/DocumentosAportacion/DocumentosAportacion.html", accion, idSolicitud, idDocumento, solicitud, documento);
	}
}
		