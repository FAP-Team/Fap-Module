
package controllers;

import messages.Messages;
import models.Documento;
import models.Solicitud;
import controllers.gen.DocumentosControllerGen;
			
public class DocumentosController extends DocumentosControllerGen {

	public static void index(String accion, Long idSolicitud, Long idDocumento){
		if (accion == null)
			accion = accion();
		if (!secure.checkAction(accion) || !permiso(accion))
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		Documento documento = null;
		if("crear".equals(accion)){
			documento = new Documento();
		}
		else if (!"borrado".equals(accion)){
			documento = DocumentosController.getDocumento(idSolicitud, idDocumento);
		}
		Solicitud solicitud = DocumentosController.getSolicitud(idSolicitud);
		renderTemplate("gen/Documentos/Documentos.html", accion, idSolicitud, solicitud, idDocumento, documento);
	}
	
}
		