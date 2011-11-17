package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import models.SolicitudGenerica;

import play.Play;
import play.utils.Java;

public class InitController {

	/**
	 * Método que inicializa lo necesario para una nueva solicitud
	 * @return Objeto que se inicializa (necesita el Id para redirigir)
	 * @throws Throwable
	 */
	public static Object inicialize() throws Throwable  {
		return InitController.invoke("inicialize");

	}
	
	private static Object invoke(String m, Object... args) throws Throwable {
		Class initController = null;
        List<Class> classes = Play.classloader.getAssignableClasses(InitController.class);
        if(classes.size() != 0) {
        	initController = classes.get(0);
        } else {
        	play.Logger.warn("No has creado ninguna clase que herede de InitController");
        	play.Logger.warn("Se utilizara el inicialize por defecto de InitController");
        	List<Class> classesSolicitud = Play.classloader.getAssignableClasses(SolicitudGenerica.class);
        	if (classesSolicitud.size() != 0) {
        		Class solicitClass = classesSolicitud.get(0);
        		SolicitudGenerica sol = (SolicitudGenerica) solicitClass.newInstance();
        		sol.estado = "borrador";
        		sol.save();
        		return sol;
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
