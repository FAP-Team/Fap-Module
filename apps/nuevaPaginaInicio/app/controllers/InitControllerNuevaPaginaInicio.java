package controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import models.ExpedienteGenerico;

import play.Play;
import play.utils.Java;

public class InitControllerNuevaPaginaInicio {

	/**
	 * Método que inicializa lo necesario para una nueva solicitud
	 * @return Objeto que se inicializa (necesita el Id para redirigir)
	 * @throws Throwable
	 */
	public static Object inicialize() throws Throwable  {
		return InitControllerNuevaPaginaInicio.invoke("inicialize");
	}
	
	private static Object invoke(String m, Object... args) throws Throwable {
		Class initController = null;
        List<Class> classes = Play.classloader.getAssignableClasses(InitControllerNuevaPaginaInicio.class);
        if(classes.size() != 0) {
        	initController = classes.get(0);
        } else {
        	play.Logger.warn("No has creado ninguna clase que herede de InitController");
        	play.Logger.warn("Se utilizara el inicialize por defecto de InitController");
        	List<Class> classesExp = Play.classloader.getAssignableClasses(ExpedienteGenerico.class);
        	if (classesExp.size() != 0) {
        		Class expClass = classesExp.get(0);
        		ExpedienteGenerico exp = (ExpedienteGenerico) expClass.newInstance();
        		//sol.estado = "borrador";
        		exp.save();
        		return exp;
        	} else {
        		// Ésto no se puede dar
        		play.Logger.fatal("No existen clases que hereden de SolicitudGenerica");
        	}
        }
        try {
        	return Java.invokeStaticOrParent(initController, m, args);
        } catch(InvocationTargetException e) {
        	throw e.getTargetException();
        }
	}
}
