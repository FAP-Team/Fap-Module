package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import play.Play;
import play.utils.Java;

public class InvokeClassController {
	
	public static <T> T invoke(Class claseInvocadora, String m, Object... args) throws Throwable {
		Class claseDelMetodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(claseInvocadora);
        if(classes.size() != 0) {
        	claseDelMetodoALlamar = classes.get(0);
        } else {
        	return (T)Java.invokeStatic(claseInvocadora, m, args);
        }
        try {
        	return (T)Java.invokeStaticOrParent(claseDelMetodoALlamar, m, args);
        } catch(InvocationTargetException e) {
        	throw e.getTargetException();
        }
	}
}
