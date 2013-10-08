package controllers;

import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.TableKeyValue;
import play.mvc.Util;
import registroresolucion.AreaResolucion;
import registroresolucion.TipoResolucion;
import services.RegistroLibroResolucionesService;
import services.async.RegistroLibroResolucionesServiceAsync;
import controllers.gen.CargarTiposAreasResolucionesControllerGen;

public class CargarTiposAreasResolucionesController extends CargarTiposAreasResolucionesControllerGen {
	
	@Inject
    public static RegistroLibroResolucionesServiceAsync registroLibroResolucionesServiceAsync;
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmCargarAreas(String cargarAreas) {
		checkAuthenticity();
		if (!permisoFrmCargarAreas("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			List<AreaResolucion> listaAreas = null;
			try {
				listaAreas = await(registroLibroResolucionesServiceAsync.leerAreas());
				TableKeyValue.deleteTable("areasResolucion");
				for (AreaResolucion area: listaAreas) {
					TableKeyValue.setValue("areasResolucion", area.idArea.toString(), area.codigo + "-" + area.descripcion, false);
				}
			} catch (Exception e) {
				play.Logger.error("Error al cargar las áreas de resolución", e);
				Messages.error("Error al cargar las áreas de resolución");
			}
		}

		if (!Messages.hasErrors()) {
			CargarTiposAreasResolucionesController.frmCargarAreasValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/CargarTiposAreasResoluciones/CargarTiposAreasResoluciones.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/CargarTiposAreasResoluciones/CargarTiposAreasResoluciones.html" + " , intentada sin éxito (Problemas de Validación)");
		CargarTiposAreasResolucionesController.frmCargarAreasRender();
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmCargarTipos(String cargarTipos) {
		checkAuthenticity();
		if (!permisoFrmCargarTipos("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			List<TipoResolucion> listaTipos = null;
			try {
				listaTipos = await(registroLibroResolucionesServiceAsync.leerTipos());
				TableKeyValue.deleteTable("tiposResolucion");
				for (TipoResolucion tipo: listaTipos) {
					TableKeyValue.setValue("tiposResolucion", tipo.idTipo.toString(), tipo.codigo + "-" + tipo.descripcion, false);
				}
			} catch (Exception e) {
				play.Logger.error("Error al cargar los tipos de resolución", e);
				Messages.error("Error al cargar los tipos de resolución");
			}
		}

		if (!Messages.hasErrors()) {
			CargarTiposAreasResolucionesController.frmCargarTiposValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/CargarTiposAreasResoluciones/CargarTiposAreasResoluciones.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/CargarTiposAreasResoluciones/CargarTiposAreasResoluciones.html" + " , intentada sin éxito (Problemas de Validación)");
		CargarTiposAreasResolucionesController.frmCargarTiposRender();
	}
}
