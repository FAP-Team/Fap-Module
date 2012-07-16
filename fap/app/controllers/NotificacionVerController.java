package controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import messages.Messages;
import models.Notificacion;
import play.mvc.Util;
import properties.FapProperties;
import controllers.gen.NotificacionVerControllerGen;

public class NotificacionVerController extends NotificacionVerControllerGen {
	
	public static void index(String accion, Long idNotificacion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/NotificacionVer/NotificacionVer.html");
		}
		checkRedirigir();

		Notificacion notificacion = null;
		if ("crear".equals(accion))
			notificacion = NotificacionVerController.getNotificacion();
		else if (!"borrado".equals(accion))
			notificacion = NotificacionVerController.getNotificacion(idNotificacion);

		log.info("Visitando página: " + "fap/Notificacion/NotificacionVer.html");
		String url = FapProperties.get("fap.notificacion.enlaceSede");
		renderTemplate("fap/Notificacion/NotificacionVer.html", accion, idNotificacion, notificacion, url);
	}

}
