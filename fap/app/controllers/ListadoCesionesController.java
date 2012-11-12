package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Cesiones;
import models.SolicitudGenerica;
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
		log.info("Visitando página: " + "gen/ListadoCesiones/ListadoCesiones.html");
		renderTemplate("gen/ListadoCesiones/ListadoCesiones.html", accion, idSolicitud, solicitud);
	}

	public static void tablatablaCesiones(Long idSolicitud) {

		java.util.List<Cesiones> rows = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where solicitud.id=?", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Cesiones> rowsFiltered = new ArrayList<Cesiones>();
		Cesiones cesionINSSR001 = null, cesionINSSA008 = null, cesionAEAT = null, cesionATC = null;
		for (Cesiones cesiones : rows) {
			if (cesiones.tipo.equals(ListaCesionesEnum.inssA008.name())&& ((cesionINSSA008 == null)||(cesiones.fechaPeticion.isAfter(cesionINSSA008.fechaPeticion)))){
				cesionINSSA008 = cesiones;
			}
			if (cesiones.tipo.equals(ListaCesionesEnum.inssR001.name())&& ((cesionINSSR001 == null)||(cesiones.fechaPeticion.isAfter(cesionINSSR001.fechaPeticion)))){
				cesionINSSR001 = cesiones;
			}
			if (cesiones.tipo.equals(ListaCesionesEnum.aeat.name())&& ((cesionAEAT == null)||(cesiones.fechaPeticion.isAfter(cesionAEAT.fechaPeticion)))){
				cesionAEAT = cesiones;
			}
			if (cesiones.tipo.equals(ListaCesionesEnum.atc.name())&& ((cesionATC == null)||(cesiones.fechaPeticion.isAfter(cesionATC.fechaPeticion)))){
				cesionATC = cesiones;
			}
		}
		if (cesionINSSA008!=null)
			rowsFiltered.add(cesionINSSA008);
		if (cesionINSSR001!=null)
			rowsFiltered.add(cesionINSSR001);
		if (cesionAEAT!=null)
			rowsFiltered.add(cesionAEAT);
		if (cesionATC!=null)
			rowsFiltered.add(cesionATC);
			
		tables.TableRenderResponse<Cesiones> response = new tables.TableRenderResponse<Cesiones>(rowsFiltered, true, true, true, "adminOrGestor", "adminOrGestor", "paginaAConfigurar", getAccion(), ids);

		renderJSON(response.toJSON("tipo", "fechaPeticion", "fechaValidez", "estado", "origen", "id"));
	}
}
