package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import models.ServicioWebInfo;

import play.Play;
import play.mvc.With;
import play.utils.Java;

public class WSController extends GenericController {

	public static ServicioWebInfo getInfoWS() {
		return null;
	}
	
	public static <T> List<ServicioWebInfo> invoke(String m, Object... args) throws Throwable {
		Class claseDelMetodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(WSController.class);
        ServicioWebInfo swi = new ServicioWebInfo();
        List<ServicioWebInfo> listaInfoWS = new ArrayList<ServicioWebInfo>();
        
        if(classes.size() != 0) {
        	for (int i = 0; i < classes.size(); i++) {
	        	claseDelMetodoALlamar = classes.get(i);
	        	if (!claseDelMetodoALlamar.getName().endsWith("Gen")) {
		        	try {
		            	swi = (ServicioWebInfo)Java.invokeStaticOrParent(claseDelMetodoALlamar, m, args);
		            	listaInfoWS.add(swi);
		            	
		            } catch(InvocationTargetException e) {
		            	throw e.getTargetException();
		            }
	        	}
        	}
        	return listaInfoWS;
        } else {
        	swi = (ServicioWebInfo)Java.invokeStatic(WSController.class, m, args);
        	listaInfoWS.add(swi);
        	return listaInfoWS;
        }
	}
}
