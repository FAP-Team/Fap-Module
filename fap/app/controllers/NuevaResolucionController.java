package controllers;

import play.mvc.Util;
import messages.Messages;
import models.ResolucionFAP;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.NuevaResolucionControllerGen;

public class NuevaResolucionController extends NuevaResolucionControllerGen {
	
	@Util
	public static Long crearLogica(ResolucionFAP resolucionFAP) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = NuevaResolucionController.getResolucionFAP();
		NuevaResolucionController.NuevaResolucionBindReferences(resolucionFAP);

		if (!Messages.hasErrors()) {
			NuevaResolucionController.NuevaResolucionValidateCopy("crear", dbResolucionFAP, resolucionFAP);
		}

		if (!Messages.hasErrors()) {
			NuevaResolucionController.crearValidateRules(dbResolucionFAP, resolucionFAP);
		}
		
		Long idResolucionFAP = null;
		if (!Messages.hasErrors()) {
			ResolucionControllerFAP.validarInicioResolucion();	
		}
		
		if (!Messages.hasErrors()) {
			dbResolucionFAP.save();
			idResolucionFAP = dbResolucionFAP.id;
			ResolucionControllerFAP.inicializaResolucion(dbResolucionFAP.id);
			log.info("Acción Crear de página: " + "gen/NuevaResolucion/NuevaResolucion.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/NuevaResolucion/NuevaResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idResolucionFAP;
	}
}
