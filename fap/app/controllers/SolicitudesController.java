
package controllers;

import play.mvc.Util;
import properties.FapProperties;
import messages.Messages;
import models.Agente;
import models.Direccion;
import models.Participacion;
import models.SolicitudGenerica;
import controllers.fap.InitController;
import controllers.fap.SecureController;
import controllers.gen.SolicitudesControllerGen;

public class SolicitudesController extends SolicitudesControllerGen {

	public static void nuevaSolicitud(){
		checkAuthenticity();
		if (permisonuevaSolicitud("update") || permisonuevaSolicitud("create")) {
			if(!validation.hasErrors()){
				try {
					SolicitudGenerica sol = (SolicitudGenerica) InitController.inicialize();
					Messages.keep();
					redirect(FapProperties.get("fap.app.firstPage")+"Controller.index", sol.id);
				} catch (Throwable e) {
					System.out.println("Error al realizar la llamada al iniciar la nueva solicitud.");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		nuevaSolicitudRender();
	}

}
