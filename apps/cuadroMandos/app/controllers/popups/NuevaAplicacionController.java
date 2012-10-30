package controllers.popups;

import java.util.Map;

import messages.Messages;
import models.Aplicacion;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.NuevaAplicacionControllerGen;

public class NuevaAplicacionController extends NuevaAplicacionControllerGen {
	
	@Util
	public static void crearValidateRules(Aplicacion dbAplicacion, Aplicacion aplicacion) {
		//Sobreescribir para validar las reglas de negocio
		
		// Comprobamos que es una dirección válida.
		if ((!dbAplicacion.urlApp.startsWith("http://")) && (!dbAplicacion.urlApp.startsWith("https://")))
			Messages.error("Ha introducido una dirección incorrecta");
	}
}
