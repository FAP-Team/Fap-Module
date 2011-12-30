
package controllers;

import java.util.Map;

import play.mvc.Util;
import properties.FapProperties;
import messages.Messages;
import models.SolicitudGenerica;
import controllers.fap.InitController;
import controllers.gen.NuevaSolicitudControllerGen;
			
public class NuevaSolicitudController extends NuevaSolicitudControllerGen {

	public static void index(String accion, Long idSolicitud){
		if (permiso("update") || permiso("create")) {
			if(!validation.hasErrors()){
				try {
					SolicitudGenerica sol = (SolicitudGenerica) InitController.inicialize();
					Messages.keep();
					redirect(FapProperties.get("fap.app.firstPage") + "Controller.index", "editar", sol.id);
				} catch (Throwable e) {
					System.out.println("Error al realizar la llamada al iniciar la nueva solicitud.");
					e.printStackTrace();
				}
			}
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		SolicitudesController.index("editar");
	}
	
}
		