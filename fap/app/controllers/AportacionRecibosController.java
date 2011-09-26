package controllers;

import java.util.List;

import models.Documento;
import play.mvc.Util;
import controllers.gen.AportacionRecibosControllerGen;

public class AportacionRecibosController extends AportacionRecibosControllerGen {

	public static void tablarecibosAportados(Long idSolicitud, Long idEntidad) {

		Long id = idSolicitud != null ? idSolicitud : idEntidad;
		List<Documento> rows = Documento
				.find("select registradas.justificante from Solicitud solicitud join solicitud.aportaciones.registradas registradas where solicitud.id=?",
						id).fetch();
		List<Documento> rowsFiltered = rows; // Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(
				rowsFiltered);
		java.util.Map<String, List<String>> valueFromTable = response
				.getValueFromTableField();

		flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include(
				"total", "rows.fechaSubida", "rows.urlDescarga", "rows.id")
				.transform(new serializer.DateTimeTransformer(),
						org.joda.time.DateTime.class);
		for (String table : valueFromTable.keySet())
			for (String field : valueFromTable.get(table))
				if ((field.equals("fechaSubida"))
						|| (field.equals("urlDescarga"))
						|| (field.equals("id")))
					flex = flex.transform(
							new serializer.ValueFromTableTransformer(table),
							"rows." + field);
		flex = flex.exclude("*");

		String serialize = flex.serialize(response);
		renderJSON(serialize);

	}

}
