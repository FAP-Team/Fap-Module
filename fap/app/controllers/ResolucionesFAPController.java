package controllers;

import java.util.Map;

import resolucion.ResolucionBase;
import messages.Messages;
import controllers.fap.AgenteController;
import controllers.gen.ResolucionesFAPControllerGen;

public class ResolucionesFAPController extends ResolucionesFAPControllerGen {

	public static void index(String accion) {
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/ResolucionesFAP/ResolucionesFAP.html");
		}

		log.info("Visitando página: " + "gen/ResolucionesFAP/ResolucionesFAP.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("gen/ResolucionesFAP/ResolucionesFAP.html", accion);
	}
	
}
