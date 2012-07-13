package controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import messages.Messages;
import play.mvc.Util;
import properties.FapProperties;
import controllers.gen.NotificacionVerControllerGen;

public class NotificacionVerController extends NotificacionVerControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void enlaceSede(Long idNotificacion, String btnEnlaceSede) {
		checkAuthenticity();
		if (!permisoEnlaceSede("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			NotificacionVerController.enlaceSedeValidateRules();
		}
		String url = "";
		if (!Messages.hasErrors()) {
			try {
				url = FapProperties.get("fap.notificacion.enlaceSede");
				if (url != null) {
					Desktop.getDesktop().browse(new URI(url));
					Messages.ok("Abierta nueva pestaña con la Sede Electrónica");
				} else{
					log.error("No existe la property de configuracion de la URL de la Sede Electrónica");
					Messages.error("Fallo al intentar acceder a la Sede Electrónica. Enlace no accesible");
				}
			} catch (Exception e) {
				Messages.error("Fallo al intentar acceder a la Sede Electrónica. Error en la construcción de la URL");
				log.error("No se ha podido abrir el enlace a la web: "+url+". "+e.getMessage());
			} 
		}
		NotificacionVerController.enlaceSedeRender(idNotificacion);
	}

}
