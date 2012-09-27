package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import play.Play;
import play.utils.Java;

import models.InfoWS;
import models.ServiciosWebApp;

public class WSController extends GenericController {

	public static ServiciosWebApp getInfoWS() {
		return null;
	}
	
	public static <T> List<ServiciosWebApp> invoke(String m, Object... args) throws Throwable {
		Class claseDelMetodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(WSController.class);
//        T w;
        ServiciosWebApp swi = new ServiciosWebApp();
        List<ServiciosWebApp> s = new ArrayList<ServiciosWebApp>();
        if(classes.size() != 0) {
        	for (int i = 0; i < classes.size(); i++) {
	        	claseDelMetodoALlamar = classes.get(i);
//	        	System.out.println(claseDelMetodoALlamar.getName().endsWith("Gen"));
	        	if (!claseDelMetodoALlamar.getName().endsWith("Gen")) {
		        	try {
		            	swi = (ServiciosWebApp)Java.invokeStaticOrParent(claseDelMetodoALlamar, m, args);
		            	s.add(swi);
		            	
		            } catch(InvocationTargetException e) {
		            	throw e.getTargetException();
		            }
	        	}
        	}
        	return s;
        } else {
        	swi = (ServiciosWebApp)Java.invokeStatic(WSController.class, m, args);
        	s.add(swi);
        	return s;
        }
        
	}
}
