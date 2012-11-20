package controllers;

import messages.Messages;
import models.AutorizacionCesion;
import models.CesionDatos;
import models.Trabajador;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.CesionDatosControllerGen;

public class CesionDatosController extends CesionDatosControllerGen {

	@Util
	public static void editarValidateRules(AutorizacionCesion dbAutorizacionCesion, AutorizacionCesion autorizacionCesion) {
		//Sobreescribir para validar las reglas de negocio

		java.util.List<Trabajador> rows = Trabajador.find("select trabajador from AutorizacionCesion autorizacionCesion join autorizacionCesion.trabajadores trabajador where autorizacionCesion.id=?", dbAutorizacionCesion.id).fetch();
		if ((rows.size() == 0) && (dbAutorizacionCesion.inssA008)){
			Messages.error("Es obligatorio indicar alguna cuenta");
		}
		
		if (!dbAutorizacionCesion.idi){
			Messages.error("Es obligatorio marca la última opción: \"A efectos,...\"");
		}
		
	}
	
}
