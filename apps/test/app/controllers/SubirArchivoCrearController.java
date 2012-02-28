
package controllers;

import messages.Messages;
import models.Solicitud;
import controllers.gen.SubirArchivoCrearControllerGen;
			
public class SubirArchivoCrearController extends SubirArchivoCrearControllerGen {

	public static void index(String accion, Long idSolicitud){
		if (accion == null)
			accion = accion();
		if (!secure.checkAction(accion) || !permiso(accion))
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		Solicitud solicitud = null;
		if("crear".equals(accion)){
			solicitud = new Solicitud();
		}
		else if (!"borrado".equals(accion)){
			solicitud = SubirArchivoCrearController.getSolicitud(idSolicitud);
		}
		renderTemplate("gen/SubirArchivoCrear/SubirArchivoCrear.html", accion, idSolicitud, solicitud);
	}
	
}
		