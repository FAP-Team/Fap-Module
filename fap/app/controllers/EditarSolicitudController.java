
package controllers;

import java.util.Map;

import properties.FapProperties;
import messages.Messages;
import models.SolicitudGenerica;
import controllers.gen.EditarSolicitudControllerGen;
			
public class EditarSolicitudController extends EditarSolicitudControllerGen {

	public static void index(String accion, Long idSolicitud){
		if (secure.check("solicitudEditable", "editable", "editar", (Map<String, Long>) tags.TagMapStack.top("idParams"), null))
			redirect(FapProperties.get("fap.app.firstPage") + "Controller.index", "editar", idSolicitud);
		else
			redirect(FapProperties.get("fap.app.firstPage") + "Controller.index", "leer", idSolicitud);
	}
	
}
		