
package config;

import java.util.List;

import org.apache.log4j.Logger;

import play.Play;
import play.modules.guice.GuiceSupport;
import secure.Secure;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import controllers.fap.InitController;


public class InjectorConfig extends GuiceSupport {
	
	private Logger logger = Logger.getLogger(InjectorConfig.class); 
	
	private static Injector injector;
	
	@Override
	protected Injector configure() {
		injector = Guice.createInjector(findAppModule()); 
		return injector;
	}

	public static Injector getInjector(){
		return injector;
	}
	
	private AbstractModule findAppModule(){
		List<Class> modules = Play.classloader.getAssignableClasses(AbstractModule.class);
		Class appModule = null;
		AbstractModule instance = null;
		for(Class module : modules){
			if(module.getName().equals("config.AppModule")){
				appModule = module;
				break;
			}
		}
		if(appModule == null){
			play.Logger.fatal("Error creando el Injector. No se encontró la clase config.AppModule. Quizás no has generado la aplicación");
		}else{
			try {
				instance = (AbstractModule)appModule.newInstance();
				logger.debug("Instanciando injector " + appModule.getName());
				play.Logger.debug("Instanciando injector " + appModule.getName());
			}catch(Exception e){
				logger.fatal("Error instanciando la clase " + appModule.getName());
				logger.fatal("Error " + e.getMessage());
			}
		}
		return instance;
	}
	
}
