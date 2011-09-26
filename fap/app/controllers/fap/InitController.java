package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
        if(classes.size() == 0) {
        	initController = InitController.class;
        } else {
        	initController = classes.get(0);
        }
        try {
        	return Java.invokeStaticOrParent(initController, m, args);
        } catch(InvocationTargetException e) {
        	throw e.getTargetException();
        }
	}
}
