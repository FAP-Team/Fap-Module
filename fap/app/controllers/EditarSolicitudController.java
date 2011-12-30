
package controllers;

import properties.FapProperties;
import messages.Messages;
import models.SolicitudGenerica;
import controllers.gen.EditarSolicitudControllerGen;
			
public class EditarSolicitudController extends EditarSolicitudControllerGen {

	public static void index(String accion, Long idSolicitud){
		redirect(FapProperties.get("fap.app.firstPage") + "Controller.index", "editar", idSolicitud);
	}
	
}
		