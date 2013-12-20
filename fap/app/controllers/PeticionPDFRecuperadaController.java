package controllers;

import play.mvc.Util;
import messages.Messages;
import models.Agente;
import controllers.fap.AgenteController;
import controllers.gen.PeticionPDFRecuperadaControllerGen;

public class PeticionPDFRecuperadaController extends PeticionPDFRecuperadaControllerGen {
	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PeticionPDFRecuperada/PeticionPDFRecuperada.html");
		}

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/PeticionPDFRecuperada/PeticionPDFRecuperada.html" + " Agente: " + logAgente);
		renderTemplate("fap/PeticionPDFRecuperada/PeticionPDFRecuperada.html", accion);
	}
	
}
