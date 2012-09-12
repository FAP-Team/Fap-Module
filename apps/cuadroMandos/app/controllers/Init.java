package controllers;

import play.Logger;
import models.Solicitud;
import controllers.fap.AgenteController;
import controllers.fap.InitController;

public class Init extends InitController {

	public static Object inicialize() {
		Solicitud solicitud = new Solicitud(AgenteController.getAgente());
		solicitud.estado = "borrador";
		
		solicitud.save();
		Logger.info("Creando solicitud " + solicitud.id);
		return solicitud;
	}
}
