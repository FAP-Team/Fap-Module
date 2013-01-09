package controllers;

import java.util.List;

import models.Documento;
import tables.TableRenderResponse;
import controllers.gen.JustificacionRecibosControllerGen;

public class JustificacionRecibosController extends JustificacionRecibosControllerGen {
	public static void tablarecibosJustificados(Long idSolicitud) {
	    List<Documento> rows = Documento
	    		.find("select registradas.registro.justificante from Solicitud solicitud " +
						  "join solicitud.justificaciones.registradas registradas " +
						  "where solicitud.id=? and registradas.registro.justificante.uri is not null",
						  idSolicitud).fetch();
	    TableRenderResponse<Documento> response = TableRenderResponse.sinPermisos(rows);
		renderJSON(response.toJSON("fechaSubida", "fechaRegistro", "tipo", "descripcionVisible", "urlDescarga", "enlaceDescargaFirmado", "id"));
	}

}
