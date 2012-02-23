package controllers;

import java.util.List;

import org.apache.ivy.util.Message;
import org.apache.log4j.Logger;

import baremacion.Evaluador;
import baremacion.IniciarBaremacion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import messages.Messages;
import play.Play;
import play.mvc.Util;
import controllers.gen.ActivarBaremacionControllerGen;

public class ActivarBaremacionController extends ActivarBaremacionControllerGen {

	private static Logger log = Logger.getLogger("Paginas");
	
	// Para iniciar cosas propias de la Baremación de cada aplicación
	public static void iniciarBaremacion(){
		
		Class invokedClass = getIniciarBaremacionClass();
		
		if (invokedClass != null){
			Method method = null;
			try {
				method = invokedClass.getDeclaredMethod("iniciar");
			} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
			if (method != null){
				try {
					method.invoke(null);
					log.info("Acción Editar de página: " + "gen/ActivarBaremacion/ActivarBaremacion.html" + " , intentada con éxito, Baremación Iniciada");
				} catch (Exception e) {} 
			} else{
				Message.error("No existe el Método apropiado para iniciar la Baremacion. El método debe llamarse 'iniciar()'");
			}
		} else{
			Message.error("No existe la Clase apropiada para iniciar la Baremacion. La clase debe extender de 'IniciarBaremacion'");
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
	
	private static Class getIniciarBaremacionClass() {
		Class invokedClass = null;
		//Busca una clase que herede del evaluador
        List<Class> assignableClasses = Play.classloader.getAssignableClasses(IniciarBaremacion.class);
        if(assignableClasses.size() > 0){
            invokedClass = assignableClasses.get(0);
        }
		return invokedClass;
	}
	
}
