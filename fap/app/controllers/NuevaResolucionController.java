package controllers;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.ResolucionFAP;

import org.joda.time.DateTime;

import com.google.inject.Inject;

import play.modules.guice.InjectSupport;
import play.mvc.Util;
import resolucion.ResolucionBase;
import services.RegistroLibroResolucionesService;
import services.async.RegistroLibroResolucionesServiceAsync;
import tags.ComboItem;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.NuevaResolucionControllerGen;

@InjectSupport
public class NuevaResolucionController extends NuevaResolucionControllerGen {
	
	@Inject
    public static RegistroLibroResolucionesServiceAsync registroLibroResolucionesServiceAsync;
	
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
			ResolucionBase resolBase = null;
			try {
				ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "validarInicioResolucion");
			} catch (Throwable e) {
				play.Logger.info("No se pudo validar el Inicio de la Resolución: "+e);
				Messages.error("No se pudo validar el Inicio de la Resolución");
			}
				
		}
		
		if (!Messages.hasErrors()) {
			dbResolucionFAP.fechaIncioPreparacion = new DateTime();
			dbResolucionFAP.save();
			idResolucionFAP = dbResolucionFAP.id;
			try {
				ResolucionBase resolBase = null;
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolBase.initResolucion(dbResolucionFAP.id);
			} catch (Throwable e) {
				play.Logger.error("No se ha podido ejecutar el método initResolucion"+e);
				Messages.error("No se ha podido ejecutar el método initResolucion");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Acción Crear de página: " + "gen/NuevaResolucion/NuevaResolucion.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/NuevaResolucion/NuevaResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idResolucionFAP;
	}
	
	public static List<ComboItem> tipoDefinidoResolucion () {
		try {
			return ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getTiposResolucion");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<ComboItem>();
	}
	
}
