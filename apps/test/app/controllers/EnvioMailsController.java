package controllers;

import java.util.HashMap;
import java.util.Map;

import messages.Messages;
import models.Solicitud;
import play.mvc.Util;
import controllers.gen.EnvioMailsControllerGen;
import emails.Mails;

public class EnvioMailsController extends EnvioMailsControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void enviarMail(Long idSolicitud, String enviarBoton) {
		checkAuthenticity();
		if (!permisoEnviarMail("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Solicitud dbSolicitud = EnvioMailsController.getSolicitud(idSolicitud);

		if (!Messages.hasErrors()) {
			EnvioMailsController.enviarMailValidateRules(dbSolicitud);
		}
		if (!Messages.hasErrors()) {
			Map<String, Object> argsVacios = new HashMap<String, Object>();
			try{
				Mails.enviar("prueba", argsVacios);
			} catch (IllegalArgumentException e){
				play.Logger.error("No se encontró el ID del mail en la base de datos");
			} catch (Exception e){
				play.Logger.error("Problemas con la plantilla del mail de presentar aportación, puede que esté mal construida");
			}
			log.info("Enviar mail de página: " + "gen/EnvioMails/EnvioMails.html" + " , intentado con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EnvioMails/EnvioMails.html" + " , intentada sin éxito (Problemas de Validación)");
		EnvioMailsController.enviarMailRender(idSolicitud);
	}

}
