package controllers;

import messages.Messages;
import models.Agente;
import models.Respuesta;
import play.mvc.Util;
import services.VerificarDatosService;
import services.VerificarDatosServiceException;
import verificacion.VerificacionUtils;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PeticionRecuperadaControllerGen;
import es.gobcan.platino.servicios.svd.*;

public class PeticionRecuperadaController extends PeticionRecuperadaControllerGen {

	public static void index(String accion, Long idRespuesta) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PeticionRecuperada/PeticionRecuperada.html");
		}

		Respuesta respuesta = null;
		if ("crear".equals(accion)) {
			respuesta = PeticionRecuperadaController.getRespuesta();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				respuesta.save();
				idRespuesta = respuesta.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			respuesta = PeticionRecuperadaController.getRespuesta(idRespuesta);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/PeticionRecuperada/PeticionRecuperada.html" + " Agente: " + logAgente);
		renderTemplate("fap/PeticionRecuperada/PeticionRecuperada.html", accion, idRespuesta, respuesta);
	}

}
