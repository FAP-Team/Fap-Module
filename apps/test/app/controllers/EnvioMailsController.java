package controllers;

import java.util.HashMap;
import java.util.Map;

import messages.Messages;
import play.mvc.Util;
import controllers.gen.EnvioMailsControllerGen;
import emails.Mails;

public class EnvioMailsController extends EnvioMailsControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void enviarMail(String enviarBoton) {
		checkAuthenticity();
		if (!permisoEnviarMail("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			EnvioMailsController.enviarMailValidateRules();
		}
		if (!Messages.hasErrors()) {
			Map<String, Object> argsVacios = new HashMap<String, Object>();
			Mails.loadFromFiles();
			Mails.enviar("pruebaInitialData", argsVacios);
			log.info("Enviar mail de página: " + "gen/EnvioMails/EnvioMails.html" + " , intentado con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EnvioMails/EnvioMails.html" + " , intentada sin éxito (Problemas de Validación)");
		EnvioMailsController.enviarMailRender();
	}

}
