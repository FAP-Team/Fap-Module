package logger;

import java.util.Arrays;
import java.util.logging.Level;

import emails.Mails;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.LocalvariablesNamesEnhancer;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport;

public class FapLogs extends PlayPlugin {
	
	@Override
	public void enhance(ApplicationClass applicationClass) throws Exception {
		if ((!applicationClass.name.contains("logger.FapLogsEnhancer")) && (!applicationClass.name.equals("logger.FapLogs"))){
			new FapLogsEnhancer().enhanceThisClass(applicationClass);
		}
	}
    
	public static void logPlay(Object[] args) {
		Throwable throwable = null;
		String pattern = "";
		if(args[0] instanceof Throwable) {
			throwable = (Throwable) args[0];
			pattern = (String) args[1];
		} else {
			pattern = (String) args[0];
		}
		
		if(Play.mode.isProd()){
			// TODO: Ver porque falla sin la siguient elínea
			// Insertamos la variable directamente, porque no funciona con el implements
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("pattern", pattern);
			try{
	    		Mails.enviar("LogFatal", pattern);
	    	} catch (IllegalArgumentException e){
	    		play.Logger.warn("No se encontró el ID del mail en la base de datos, por lo que no se pudo enviar por correo el error FATAL");
	    	}
		}
		
		if(throwable != null) {
			Logger.fatal(throwable, pattern, handleLogArgs(args, 2));
		}
		else {
			Logger.fatal(pattern, handleLogArgs(args, 1));
		}
	}
	
	public static void logLog4j(Object[] args) {
		Throwable throwable = null;
		String pattern = "";
		pattern = (String) args[0];
		if(args.length == 2) {
			throwable = (Throwable) args[1];
		}
		if(Play.mode.isProd()){
			// TODO: Ver porque falla sin la siguient elínea
			// Insertamos la variable directamente, porque no funciona con el implements
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("pattern", pattern);
			try{
	    		Mails.enviar("LogFatal", pattern);
	    	} catch (IllegalArgumentException e){
	    		play.Logger.warn("No se encontró el ID del mail en la base de datos, por lo que no se pudo enviar por correo el error FATAL");
	    	}
		}
		if(throwable != null) {
			Logger.fatal(throwable, pattern);
		}
		else{
			Logger.fatal(pattern);
		}
	}

	private static Object[] handleLogArgs(Object[] original, int skip) {
		Object[] kept = Arrays.copyOfRange(original, skip, original.length - 1);
		if(original[original.length - 1] instanceof Object[]) // flatten
			kept = concat(kept, (Object[])original[original.length - 1]);
		else kept = concat(kept, new Object[] { original[original.length - 1]});
		return kept;
	}

	private static Object[] concat(Object[] o1, Object[] o2) {
		Object[] result = new Object[o1.length + o2.length];
		for(int i = 0; i < o1.length; i++)
			result[i] = o1[i];
		for(int j = 0; j < o2.length; j++)
			result[o1.length+j] = o2[j];
		return result;
	}    

	
}
