package controllers;

import messages.Messages;
import models.ResolucionFAP;

import org.joda.time.DateTime;

import com.google.inject.Inject;

import play.modules.guice.InjectSupport;
import play.mvc.Util;
import services.RegistroLibroResolucionesService;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.NuevaResolucionControllerGen;

@InjectSupport
public class NuevaResolucionController extends NuevaResolucionControllerGen {
	
	@Inject
    public static RegistroLibroResolucionesService registroLibroResolucionesService;
	
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
			dbResolucionFAP.fechaIncioPreparacion = new DateTime();
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
