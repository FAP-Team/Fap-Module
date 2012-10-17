
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

import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosEvaluacionEnum;

@With({SecureController.class, AgenteController.class})
public class ConsultarEvaluacionesController extends GenericController {
	
	@Finally(only="index")
	public static void end(){
		Messages.deleteFlash();
	}
	
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
		if(secure.checkGrafico("listaEvaluaciones", "visible", "leer", null, null)){
			java.util.List<Evaluacion> rows = Evaluacion.find( "select evaluacion from Evaluacion evaluacion order by evaluacion.solicitud.expedienteAed.idAed" ).fetch();
			List<Evaluacion> rowsFiltered = rows; //Tabla sin permisos, no filtra
			tables.TableRenderNoPermisos<Evaluacion> response = new tables.TableRenderNoPermisos<Evaluacion>(rowsFiltered);
			renderJSON(response.toJSON("solicitud.expedienteAed.idAed", "solicitud.solicitante.numeroId", "solicitud.solicitante.nombreCompleto", "estado", "id"));
		}else{
			forbidden();
		}
	}
	
	public static void aceptar(Long idEvaluacion){
		if(secure.checkGrafico("listaEvaluaciones", "editable", "leer", null, null)){
			Evaluacion eval = Evaluacion.findById(idEvaluacion);
			if (eval != null) {
				eval.estado = EstadosEvaluacionEnum.enTramite.name();
				eval.save();
			}
		}else{
			forbidden();
		}
	}
		
	
	public static void rechazar(Long idEvaluacion){
		if(secure.checkGrafico("listaEvaluaciones", "editable", "leer", null, null)){
			Evaluacion eval = Evaluacion.findById(idEvaluacion);
			if (eval != null) {
				eval.estado = EstadosEvaluacionEnum.rechazada.name();
				eval.save();
			}
		}else{
			forbidden();
		}
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void botonEvaluacionesFinalizadas(String btnEvaluacionesFinalizadas) {
		checkAuthenticity();
		if (!Messages.hasErrors()) {
			String accion = "editable";
			renderTemplate("fap/EvaluacionesFinalizadas/EvaluacionesFinalizadas.html", accion);
		}
	}
}
