package controllers;

import java.util.List;
import java.util.Map;

import play.mvc.Util;

import resolucion.ResolucionBase;

import messages.Messages;
import models.Resolucion;
import models.SolicitudGenerica;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.EditarResolucionControllerGen;

public class EditarResolucionController extends EditarResolucionControllerGen {

	/**
	 * Expedientes que se muestran en la tabla para poder seleccionar
	 */
	public static void tablatablaExpedientes() {
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		// Obtenemos el objeto "ResolucionBase"
		ResolucionBase resolBase = ResolucionControllerFAP.getResolucionObject(ids.get("idResolucion"));
		
		java.util.List<SolicitudGenerica> rows = resolBase.getSolicitudesAResolver();
		
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void prepararResolucion(Long idResolucion, String btnPrepararResolucion) {
		checkAuthenticity();
		if (!permisoPrepararResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			Resolucion resolucion = EditarResolucionController.getResolucion(idResolucion);
			ResolucionBase resolBase = ResolucionControllerFAP.getResolucionObject(idResolucion);
			resolBase.setLineasDeResolucion(idResolucion);
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.prepararResolucionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.prepararResolucionRender(idResolucion);
	}
	
}
