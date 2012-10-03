package controllers;

import java.util.List;

import org.apache.ivy.util.Message;
import org.apache.log4j.Logger;

import baremacion.BaremacionFAP;
import baremacion.Evaluador;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import messages.Messages;
import models.Evaluacion;
import models.TipoEvaluacion;
import play.Play;
import play.mvc.Util;
import services.BaremacionService;
import utils.BaremacionUtils;
import controllers.gen.ActivarBaremacionControllerGen;

public class ActivarBaremacionController extends ActivarBaremacionControllerGen {

	private static Logger log = Logger.getLogger("Paginas");
	
	// Para iniciar cosas propias de la Baremación de cada aplicación
	public static void iniciarBaremacion(){
		
		Class invokedClass = getBaremacionFAPClass();
		
		if (invokedClass != null){
			Method method = null;
			try {
				method = invokedClass.getDeclaredMethod("iniciarBaremacion");
			} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
			if (method != null){
				try {
					method.invoke(null);
					TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
					tipoEvaluacion.estado="iniciada";
					tipoEvaluacion.save();
					log.info("Acción Editar de página: " + "gen/ActivarBaremacion/ActivarBaremacion.html" + " , intentada con éxito, Baremación Iniciada");
				} catch (Exception e) {} 
			} else{
				play.Logger.error("No existe el Método apropiado para iniciar la Baremacion. El método debe llamarse 'iniciar()'");
				Messages.error("No existe el Método apropiado para iniciar la Baremacion. El método debe llamarse 'iniciar()'");
			}
		} else{
			play.Logger.error("No existe la Clase apropiada para iniciar la Baremacion. La clase debe extender de 'IniciarBaremacion'");
			Messages.error("No existe la Clase apropiada para iniciar la Baremacion. La clase debe extender de 'IniciarBaremacion'");
		}
		ActivarBaremacionController.activarFormBaremacionRender();
	}
	
	// Métodos sobreescritos del Generado
	
	public static void activarFormBaremacion() {
		checkAuthenticity();
		if (!permisoActivarFormBaremacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			ActivarBaremacionController.activarFormBaremacionValidateRules();
		}
		ActivarBaremacionController.iniciarBaremacion();
	}

	@Util
	public static void activarFormBaremacionRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Baremación Iniciada correctamente");
			Messages.keep();
			redirect("ActivarBaremacionController.index", "editar");
		}
		Messages.keep();
		redirect("ActivarBaremacionController.index", "editar");
	}
	
	private static Class getBaremacionFAPClass() {
		Class invokedClass = null;
		//Busca una clase que herede del evaluador
        List<Class> assignableClasses = Play.classloader.getAssignableClasses(BaremacionFAP.class);
        if(assignableClasses.size() > 0){
            invokedClass = assignableClasses.get(0);
        } else {
        	invokedClass = BaremacionFAP.class;
        }
		return invokedClass;
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void iniciarTipoEvaluacionFormBaremacion(String iniciarTipoEvaluacionBoton) {
		checkAuthenticity();
		if (!permisoIniciarTipoEvaluacionFormBaremacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			TipoEvaluacion tipoEvaluacion = null;
			if((TipoEvaluacion.count() == 0) && (new File(Play.applicationPath+"/conf/initial-data/tipoEvaluacion.json").exists())){
				tipoEvaluacion = BaremacionService.loadTipoEvaluacionFromJson("/conf/initial-data/tipoEvaluacion.json");
				tipoEvaluacion.estado = "cargada";
				tipoEvaluacion.save();
				BaremacionUtils.actualizarParametrosVariables(tipoEvaluacion);
				log.info("Tipo de Baremación cargada correctamente desde fichero por primera vez");
			} else {
				Messages.error("Tipo de Baremación no fue cargada con éxito debido a que no existe el fichero pertienente, o ya había sido cargada previamente");
				log.error("Tipo de Baremación no fue cargada con éxito debido a que no existe el fichero pertienente, o ya había sido cargada previamente");
			}
		}

		if (!Messages.hasErrors()) {
			ActivarBaremacionController.iniciarTipoEvaluacionFormBaremacionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/ActivarBaremacion/ActivarBaremacion.html" + " , intentada con éxito, inicializando");
		} else
			log.info("Acción Editar de página: " + "gen/ActivarBaremacion/ActivarBaremacion.html" + " , intentada sin éxito (Problemas de Validación), al inicializar");
		ActivarBaremacionController.iniciarTipoEvaluacionFormBaremacionRender();
	}
	
	@Util
	public static void iniciarTipoEvaluacionFormBaremacionRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Tipo de Baremación cargada correctamente desde fichero por primera vez");
			Messages.keep();
			redirect("ActivarBaremacionController.index", "editar");
		}
		Messages.keep();
		redirect("ActivarBaremacionController.index", "editar");
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void actualizarFormBaremacion(String actualizarBaremacionBoton) {
		checkAuthenticity();
		if (!permisoActualizarFormBaremacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			if (TipoEvaluacion.count() > 0){
				TipoEvaluacion tipoEvaluacion = (TipoEvaluacion) TipoEvaluacion.findAll().get(0);
				BaremacionUtils.actualizarParametrosVariables(tipoEvaluacion);
				Evaluacion evaluacion = (Evaluacion) Evaluacion.findAll().get(0);
				evaluacion.actualizar(tipoEvaluacion);
				evaluacion.save();
				log.info("Tipo de Baremación y evaluaciones de ese tipo actualizada correctamente desde fichero");
			}
		}

		if (!Messages.hasErrors()) {
			ActivarBaremacionController.actualizarFormBaremacionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/ActivarBaremacion/ActivarBaremacion.html" + " , intentada con éxito, actualizando");
		} else
			log.info("Acción Editar de página: " + "gen/ActivarBaremacion/ActivarBaremacion.html" + " , intentada sin éxito (Problemas de Validación), al actualizar");
		ActivarBaremacionController.actualizarFormBaremacionRender();
	}
	
	@Util
	public static void actualizarFormBaremacionRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Tipo de Baremación actualizada correctamente desde fichero");
			Messages.keep();
			redirect("ActivarBaremacionController.index", "editar");
		}
		Messages.keep();
		redirect("ActivarBaremacionController.index", "editar");
	}
	
}
