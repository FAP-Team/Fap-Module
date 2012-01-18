
package config;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import play.Play;
import play.modules.guice.GuiceSupport;
import security.Secure;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import controllers.fap.InitController;


public class InjectorConfig extends GuiceSupport {

	private Logger logger = Logger.getLogger(InjectorConfig.class); 
	
	private static Injector injector;
	
	@Override
	protected Injector configure() {
		injector = Guice.createInjector(modulesToLoad()); 
		return injector;
	}

	public static Injector getInjector(){
		return injector;
	}
	
	private List<AbstractModule> modulesToLoad(){
		List<Class> modules = Play.classloader.getAssignableClasses(AbstractModule.class);
		
		if(modules.isEmpty()){
			throw new IllegalStateException("No hay ninguna clase que extienda de AbstractModule");
		}
		
		Class appModule = null;
		for(Class module : modules){
			if(module.getName().equals("config.AppModule")){
				appModule = module;
				break;
			}
		}
		
		//Si está el modulo config.AppModule únicamente se carga este
		if(appModule != null){
			modules.clear();
			modules.add(appModule);
		}
		
		List<AbstractModule> modulesInstances = new ArrayList<AbstractModule>();
		for(Class module : modules){
			try {
				modulesInstances.add((AbstractModule) module.newInstance());
				play.Logger.info("Instanciado módulo " + module.getName());
			}catch(Exception e){
				logger.debug("Error instanciando módulo " + module.getName());
			}
		}
		
		return modulesInstances;
	}
	
}
