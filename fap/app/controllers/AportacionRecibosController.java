package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Documento;
import play.mvc.Util;
import tables.TableRenderResponse;
import controllers.gen.AportacionRecibosControllerGen;

public class AportacionRecibosController extends AportacionRecibosControllerGen {

	public static void tablarecibosAportados(Long idSolicitud) {
	    List<Documento> rows = Documento
	    		.find("select registradas.registro.justificante from Solicitud solicitud " +
						  "join solicitud.aportaciones.registradas registradas " +
						  "where solicitud.id=? and registradas.registro.justificante.uri is not null",
						  idSolicitud).fetch();
	    TableRenderResponse<Documento> response = TableRenderResponse.sinPermisos(rows);
		renderJSON(response.toJSON("fechaSubida", "fechaRegistro", "tipo", "descripcionVisible", "urlDescarga", "enlaceDescargaFirmado", "uri",  "id"));
	}

}
