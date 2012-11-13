
package controllers.fap;

import play.*;
import play.mvc.*;
import properties.FapProperties;
import baremacion.BaremacionFAP;
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
import java.lang.reflect.Type;

import javax.inject.Inject;

import com.google.gson.reflect.TypeToken;

import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosEvaluacionEnum;

@With(CheckAccessController.class)
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
				}
			}
		}else{
			forbidden();
		}
	}
	
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
		if (FapProperties.getBoolean("fap.baremacion.evaluacion.permitirRecargarConceptos")) {
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

	
//	@Util
//	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
//	public static void botonEvaluacionesFinalizadas(String btnEvaluacionesFinalizadas) {
//		checkAuthenticity();
//		if (!Messages.hasErrors()) {
//			String accion = "editable";
//			renderTemplate("fap/EvaluacionesFinalizadas/EvaluacionesFinalizadas.html", accion);
//		}
//	}
	
}
