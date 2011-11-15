
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
		renderTemplate( "fap/Baremacion/consultarEvaluaciones.html");
	}

	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "ConsultarEvaluacionesControllerGen");
	}

	public static void tablatablaEvaluacionesAsignadas(Long idEvaluacion, Long idEntidad){

		if(Evaluacion.count() == 0){
				Evaluacion evaluacion = new Evaluacion();
				Random ran = new Random();
				evaluacion.solicitud.expedienteAed.asignarIdAed();
				evaluacion.solicitud.solicitante.tipo = "fisica";
				evaluacion.solicitud.solicitante.fisica.nip = new Nip();
				evaluacion.solicitud.solicitante.fisica.nip.valor = ran.nextLong()+"";
				evaluacion.solicitud.solicitante.fisica.nombre = "AAAA";
				evaluacion.solicitud.solicitante.fisica.primerApellido = "AAAA";
				evaluacion.solicitud.solicitante.fisica.segundoApellido = "AAAA";
				evaluacion.save();
		}
		
		//TODO Filtrar las evaluaciones que tiene asignada	
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
