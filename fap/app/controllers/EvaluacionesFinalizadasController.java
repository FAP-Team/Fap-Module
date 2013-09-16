package controllers;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Evaluacion;
import models.TipoEvaluacion;
import controllers.fap.FichaEvaluadorController;
import controllers.gen.EvaluacionesFinalizadasControllerGen;
import enumerado.fap.gen.EstadosEvaluacionEnum;

public class EvaluacionesFinalizadasController extends EvaluacionesFinalizadasControllerGen {
	
	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EvaluacionesFinalizadas/EvaluacionesFinalizadas.html");
		}

		log.info("Visitando página: " + "fap/EvaluacionesFinalizadas/EvaluacionesFinalizadas.html");
		renderTemplate("fap/EvaluacionesFinalizadas/EvaluacionesFinalizadas.html", accion);
	}
	
	public static void tablatablaEvaluacionesFinalizadas() {
		
		java.util.List<Evaluacion> rows = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.estado=?", EstadosEvaluacionEnum.evaluada.name()).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Evaluacion> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Evaluacion> response = new tables.TableRenderResponse<Evaluacion>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("totalCriterios", "solicitud.expedienteAed.idAed", "estado", "solicitud.solicitante.numeroId", "solicitud.solicitante.nombreCompleto", "estado", "id"));
	}
	
//	public static void editarEvaluacion(Long idEvaluacion, String accion){
//		if ("editar".equals(accion)){
//			Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
//			evaluacion.estado = EstadosEvaluacionEnum.enTramite.name();
//			evaluacion.save();
//			FichaEvaluadorController.index(idEvaluacion, accion);
//		} else {
//			FichaEvaluadorController.index(idEvaluacion, accion);
//		}
//	}
	
}
