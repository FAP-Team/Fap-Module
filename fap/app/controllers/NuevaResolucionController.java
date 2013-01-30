package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import play.modules.guice.InjectSupport;
import play.mvc.Util;
import registroresolucion.AreaResolucion;
import registroresolucion.TipoResolucion;
import services.RegistroLibroResolucionesService;
import services.RegistroLibroResolucionesServiceException;
import tags.ComboItem;
import messages.Messages;
import models.ResolucionFAP;
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
	
	public static List<ComboItem> areasResolucion() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<AreaResolucion> listaAreas = null;
		try {
			listaAreas = registroLibroResolucionesService.leerAreas();
		} catch (RegistroLibroResolucionesServiceException e) {
			play.Logger.error("Error al obtener las áreas de resolución");
		}
		for (AreaResolucion area: listaAreas) {
			result.add(new ComboItem(area.idArea, area.codigo + " - " +area.descripcion));
		}
		return result;
	}
	
	public static List<ComboItem> tiposResolucion() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<TipoResolucion> listaTipos = null;
		try {
			listaTipos = registroLibroResolucionesService.leerTipos();
		} catch (RegistroLibroResolucionesServiceException e) {
			play.Logger.error("Error al obtener las áreas de resolución");
		}
		for (TipoResolucion tipo: listaTipos) {
			result.add(new ComboItem(tipo.idTipo, tipo.codigo + " - " + tipo.descripcion));
		}
		return result;
	}
	
}
