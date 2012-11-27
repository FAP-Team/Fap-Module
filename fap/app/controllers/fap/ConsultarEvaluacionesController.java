
package controllers.fap;

import play.*;
import play.mvc.*;
import properties.FapProperties;
import baremacion.BaremacionFAP;
import controllers.PaginaCEconomicosEvaluadosController;
import controllers.fap.*;
import security.Secure;
import services.BaremacionService;
import tags.ReflectionUtils;
import utils.BaremacionUtils;
import utils.JsonUtils;
import validation.*;
import models.*;

import java.security.Permission;
import java.util.*;

import messages.Messages;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.inject.Inject;

import com.google.gson.reflect.TypeToken;

import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosEvaluacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;

@With({SecureController.class, AgenteController.class, CheckAccessController.class})
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
	
	@Util
	public static void aceptar(Long idEvaluacion){
		if(secure.checkGrafico("listaEvaluaciones", "editable", "leer", null, null)){
			Evaluacion eval = Evaluacion.findById(idEvaluacion);
			if (eval != null) {
				try {
					BaremacionFAP.setOficialEvaluacion(eval.solicitud.id, idEvaluacion);
					eval.estado = EstadosEvaluacionEnum.enTramite.name();
					BaremacionService.calcularTotales(eval);
					eval.save();
					FichaEvaluadorController.index(idEvaluacion, "editar");
				} catch (Exception e) {
					Messages.error("Error generando el documento de solicitud para ver en evaluación. No se ha podido Iniciar esta Evaluación.");
	                play.Logger.error("Error generando el de solicitud para ver en evaluación, no se ha ACEPTADO la evaluación: "+e.getMessage());
	                Messages.keep();
	                index();
				}
			}
		}else{
			forbidden();
		}
	}
	
	@Util
	public static void reevaluar(Long idEvaluacion){
		if(secure.checkGrafico("listaEvaluaciones", "editable", "leer", null, null)){
			Evaluacion eval = Evaluacion.findById(idEvaluacion);
			if (eval != null) {
				eval.estado = EstadosEvaluacionEnum.enTramite.name();
				BaremacionService.calcularTotales(eval);
				eval.save();
				FichaEvaluadorController.index(idEvaluacion, "editar");
			}
		}else{
			forbidden();
		}
	}
		
	@Util
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
	public static void recargarCE(Long idEvaluacion) {
		if(secure.checkGrafico("listaEvaluaciones", "editable", "leer", null, null)){
			Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
			TipoEvaluacion tipoEvaluacion = evaluacion.tipo;
				
			eliminarCEAnterior(evaluacion, tipoEvaluacion);
			crearCENuevo(evaluacion, tipoEvaluacion);
			play.Logger.info("Se han recargado los conceptos económicos");
			Messages.info("Se han recargado los conceptos económicos");
			Messages.keep();
			aceptar(idEvaluacion);
				
		} else {
			forbidden();
		}
	}
	
	@Util
	public static void eliminarCEAnterior(Evaluacion evaluacion, TipoEvaluacion tipoEvaluacion) {
		play.Logger.info("Eliminando los conceptos económicos de la evaluación "+ evaluacion.id);
		int sizeCE = evaluacion.ceconomicos.size();
		for (int i = 0; i < sizeCE; i++) {
			int sizeValores = evaluacion.ceconomicos.get(0).valores.size();
			Long idCEconomico = evaluacion.ceconomicos.get(0).id;
			for (int j = 0; j < sizeValores; j++) {
				Long idValoresCE = evaluacion.ceconomicos.get(0).valores.get(0).id;
				evaluacion.ceconomicos.get(0).valores.remove(0);
				evaluacion.save();
				ValoresCEconomico.delete("delete from ValoresCEconomico where id=?", idValoresCE);
				CEconomico c = new CEconomico();
				c.valores.remove(idValoresCE);
			}
			evaluacion.ceconomicos.remove(0);
			evaluacion.save();
			CEconomico.delete("delete from CEconomico where id=?", idCEconomico);
			evaluacion.save();
		}
	}
	
	@Util
	public static void crearCENuevo(Evaluacion evaluacion, TipoEvaluacion tipoEvaluacion) {
		play.Logger.info("Creando los conceptos económicos de la evaluación "+ evaluacion.id+ " a partir de los de la solicitud " + evaluacion.solicitud.id);
		for (TipoCEconomico tCEconomico : tipoEvaluacion.ceconomicos) {
			CEconomico cEconomico = new CEconomico();
			cEconomico.tipo = tCEconomico;
			for (int i = 0; i < tipoEvaluacion.duracion; i++) {
				ValoresCEconomico vCEconomico = new ValoresCEconomico(i);
				cEconomico.valores.add(vCEconomico);
			}
			evaluacion.ceconomicos.add(cEconomico);
		}
		
		for(CEconomico ceconomicoS : evaluacion.solicitud.ceconomicos) {
			for(CEconomico ceconomicoE : evaluacion.ceconomicos) {
				if (ceconomicoE.tipo.nombre.equals(ceconomicoS.tipo.nombre)) {
					for (int i = 0; i < tipoEvaluacion.duracion; i++) {
						ceconomicoE.valores.get(i).valorSolicitado = ceconomicoS.valores.get(i).valorSolicitado;
					}
					break;
				}
			}
			if (ceconomicoS.tipo.tipoOtro) {
				for (CEconomicosManuales ceconomicoManual: ceconomicoS.otros) {
					for(CEconomico ceconomicoE : evaluacion.ceconomicos) {
						if (ceconomicoE.tipo.nombre.equals(ceconomicoManual.tipo.nombre)) {
							for (int i = 0; i < tipoEvaluacion.duracion; i++) {
								ceconomicoE.valores.get(i).valorSolicitado = ceconomicoManual.valores.get(i).valorSolicitado;
							}
							break;
						}
					}
				}
			}
		}
		
		// Cambiar estado
		//evaluacion.estado = null;
		//evaluacion.save();
	}

	
	@Util
	public static void botonFinalizarEvaluaciones(String btnEvaluacionesFinalizadas) {
		checkAuthenticity();

		if (!Messages.hasErrors()) {
			Class invokedClass = null;
			//Busca una clase que herede de BaremacionFAP
	        List<Class> assignableClasses = Play.classloader.getAssignableClasses(BaremacionFAP.class);
	        if(assignableClasses.size() > 0) {
	            invokedClass = assignableClasses.get(0);
	        } else {
	        	invokedClass = BaremacionFAP.class;
	        }
	        if (invokedClass != null) {
				Method method = null;
				try {
					method = invokedClass.getDeclaredMethod("finalizarEvaluaciones");
				} catch (Exception ex) {
					invokedClass = BaremacionFAP.class;
					if (invokedClass != null) {
						method = null;
						try {
							method = invokedClass.getDeclaredMethod("finalizarEvaluaciones");
						} catch (Exception e) {
							play.Logger.error("Error g001: No se ha podido encontrar el método finalizarEvaluaciones de la clase BaremacionFAP");
							Messages.error("Error interno g001. No se ha podido Guardar correctamente");
						}
					}
				}
				if (!Messages.hasErrors()) {
					if (method != null) {
						try {
							method.invoke(ConsultarEvaluacionesController.class);
						} catch (Exception e) {
							play.Logger.error("Error g002: No se ha podido invocar el método finalizarEvaluaciones de la clase BaremacionFAP");
							Messages.error("Error interno g002. No se ha podido Guardar correctamente");
						} 
					} else {
						play.Logger.error("Error g003: No existe el Método apropiado para validar los CEconomicos. El método debe llamarse 'finalizarEvaluaciones()'");
						Messages.error("Error interno g003. No se ha podido Guardar correctamente");
					}
				}
			} else {
				play.Logger.error("Error g004: No existe la Clase apropiada para iniciar la Baremacion. La clase debe extender de 'BaremacionFAP'");
				Messages.error("Error interno g004. No se ha podido Guardar correctamente");
			}
		}
		index();
	}
	
}
