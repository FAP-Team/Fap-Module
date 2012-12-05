package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import peticionCesion.PeticionBase;
import utils.CesionesUtils;

import messages.Messages;
import models.Cesion;
import models.Cesiones;
import models.SolicitudGenerica;
import models.TableKeyValue;
import controllers.fap.PeticionFapController;
import controllers.gen.ListadoCesionesControllerGen;
import enumerado.fap.gen.ListaCesionesEnum;

public class ListadoCesionesController extends ListadoCesionesControllerGen {

	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/ListadoCesiones/ListadoCesiones.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = ListadoCesionesController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = ListadoCesionesController.getSolicitudGenerica(idSolicitud);

		java.util.List<Cesiones> rows = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where solicitud.id=?", idSolicitud).fetch();
		if (rows.size() == 0)
			Messages.info("No hay cesiones de datos asociadas a esta solicitud");
		log.info("Visitando página: " + "fap/ListadoCesiones/ListadoCesiones.html");
		renderTemplate("gen/ListadoCesiones/ListadoCesiones.html", accion, idSolicitud, solicitud);
	}

	public static void tablatablaCesiones(Long idSolicitud) {
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Cesiones> rowsFiltered = new ArrayList<Cesiones>();

		java.util.List<String> tipos = null;
		try {
			tipos = PeticionFapController.invoke(PeticionFapController.class, "getTiposCesiones");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		//Ordenar lista de cesiones por fecha y tipo

		for (String tipo : tipos) {
			java.util.List<Cesiones> rows = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where solicitud.id=? and cesiones.tipo=?", idSolicitud, tipo).fetch();
			//TODO: Mejorar la consulta de la última cesión de cada tipo
			CesionesUtils.ordenarTiposCesiones(rows); //Lista ordenada por fecha mas próxima a más lejana
			if (!rows.isEmpty())
				rowsFiltered.add(rows.get(0)); //El primero es el más reciente
		}		
		tables.TableRenderResponse<Cesiones> response = new tables.TableRenderResponse<Cesiones>(rowsFiltered, true, true, true, "adminOrGestor", "adminOrGestor", "paginaAConfigurar", getAccion(), ids);
		renderJSON(response.toJSON("tipo", "fechaPeticion", "fechaValidez", "estado", "origen", "id"));
	}
}
