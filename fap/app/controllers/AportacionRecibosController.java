package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Documento;
import play.mvc.Util;
import controllers.gen.AportacionRecibosControllerGen;

public class AportacionRecibosController extends AportacionRecibosControllerGen {

	public static void tablarecibosAportados(Long idSolicitud) {
		List<Documento> rows = Documento
				.find("select registradas.justificante from Solicitud solicitud join solicitud.aportaciones.registradas registradas where solicitud.id=?",
						idSolicitud).fetch();
		//List<Documento> rowsFiltered = rows; // Tabla sin permisos, no filtra
		
		Map<String, Long> ids = new HashMap<String, Long>();
		List<Documento> rowsFiltered = new ArrayList<Documento>();
		for(Documento documento: rows){
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("doc", documento);
			if (secure.checkGrafico("aportacionNoNull", "visible", "editar", ids, vars)) {
				rowsFiltered.add(documento);
			}
		}

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(tablarecibosAportadosPermisos(rowsFiltered));
		java.util.Map<String, List<String>> valueFromTable = response.getValueFromTableField();

		flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include(
			"rows.objeto.fechaSubida",
			"rows.objeto.urlDescarga",
			"rows.objeto.id",
			"rows.permisoLeer",
			"rows.permisoEditar",
			"rows.permisoBorrar"
			).transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class);
		for (String table : valueFromTable.keySet())
			for (String field : valueFromTable.get(table))
				if ((field.equals("fechaSubida"))
						|| (field.equals("urlDescarga"))
						|| (field.equals("id")))
					flex = flex.transform(
							new serializer.ValueFromTableTransformer(table),
							"rows.objeto." + field);
		flex = flex.exclude("*");

		String serialize = flex.serialize(response);
		renderJSON(serialize);
	}

}
