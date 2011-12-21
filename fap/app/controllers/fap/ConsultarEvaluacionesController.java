
package controllers.fap;

import play.*;
import play.mvc.*;
import controllers.fap.*;
import security.Secure;
import tags.ReflectionUtils;
import validation.*;
import models.*;

import java.security.Permission;
import java.util.*;
import messages.Messages;
import java.lang.reflect.Field;

import javax.inject.Inject;

@With({SecureController.class, AgenteController.class})
public class ConsultarEvaluacionesController extends GenericController {
	
	@Inject
	protected static Secure secure;
	
	public static void index(){
		renderTemplate("fap/Baremacion/consultarEvaluaciones.html");
	}
	
	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "ConsultarEvaluacionesControllerGen");
	}

	public static void tablatablaEvaluacionesAsignadas(Long idEvaluacion, Long idEntidad){		
		//TODO Filtrar las evaluaciones que tiene asignada	
		if(secure.check("listaEvaluaciones", "read", null, null)){
			java.util.List<Evaluacion> rows = Evaluacion.find( "select evaluacion from Evaluacion evaluacion order by evaluacion.solicitud.expedienteAed.idAed" ).fetch();
			List<Evaluacion> rowsFiltered = rows; //Tabla sin permisos, no filtra
			tables.TableRenderResponse<Evaluacion> response = new tables.TableRenderResponse<Evaluacion>(rowsFiltered);
			renderJSON(response.toJSON("solicitud.expedienteAed.idAed", "solicitud.solicitante.numeroId", "solicitud.solicitante.nombreCompleto", "estado", "id"));
		}else{
			forbidden();
		}
	}
	
	public static void aceptar(Long idEvaluacion){
		if(secure.check("listaEvaluaciones", "update", null, null)){
			Evaluacion eval = Evaluacion.findById(idEvaluacion);
			if (eval != null) {
				eval.estado = "EnTramite";
				eval.save();
			}
		}else{
			forbidden();
		}
	}
		
	
	public static void rechazar(Long idEvaluacion){
		if(secure.check("listaEvaluaciones", "update", null, null)){
			Evaluacion eval = Evaluacion.findById(idEvaluacion);
			if (eval != null) {
				eval.estado = "Rechazada";
				eval.save();
			}
		}else{
			forbidden();
		}
	}
}
