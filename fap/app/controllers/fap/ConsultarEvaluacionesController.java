
package controllers.fap;

import play.*;
import play.mvc.*;
import controllers.fap.*;
import tags.ReflectionUtils;
import validation.*;
import models.*;
import java.util.*;
import messages.Messages;
import java.lang.reflect.Field;


public class ConsultarEvaluacionesController extends GenericController {

	public static void index(Long idSolicitud){
		renderTemplate( "fap/ConsultarEvaluaciones/ConsultarEvaluaciones.html");
	}

	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "ConsultarEvaluacionesControllerGen");
	}

	public static void tablatablaEvaluacionesAsignadas(Long idEvaluacion, Long idEntidad){

		Evaluacion eva = new Evaluacion();
		Random ran = new Random();

		eva.solicitud.expedienteAed.asignarIdAed();
		eva.solicitud.solicitante.tipo = "fisica";
		eva.solicitud.solicitante.fisica.nip.valor = ran.nextLong()+"";
		eva.solicitud.solicitante.fisica.nombre = "AAAA";
		eva.solicitud.solicitante.fisica.primerApellido = "AAAA";
		eva.solicitud.solicitante.fisica.segundoApellido = "AAAA";
		eva.save();
		
		
		Long id = idEvaluacion != null? idEvaluacion : idEntidad;
		java.util.List<Evaluacion> rows = Evaluacion.find( "select evaluacion from Evaluacion evaluacion" ).fetch();

		List<Evaluacion> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Evaluacion> response = new tables.TableRenderResponse<Evaluacion>(rowsFiltered);
		renderJSON(response.toJSON("solicitud.expedienteAed.idAed", "solicitud.solicitante.numeroId", "solicitud.solicitante.nombreCompleto", "estado", "id"));

	}
	
	public static void aceptar(Long idEvaluacion){
		Evaluacion eval = Evaluacion.findById(idEvaluacion);
		if (eval != null) {
			eval.estado = "EnTramite";
			eval.save();
		}

	}
	
	public static void rechazar(Long idEvaluacion){
		Evaluacion eval = Evaluacion.findById(idEvaluacion);
		if (eval != null) {
			eval.estado = "Rechazada";
			eval.save();
		}
	}


}
