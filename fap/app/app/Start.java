package app;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

import emails.Mails;

import messages.Messages;
import models.*;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClassloader;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Router;
import models.SolicitudGenerica;
import play.test.Fixtures;
import play.vfs.VirtualFile;
import properties.FapProperties;
import properties.Properties;
import services.BaremacionService;

@OnApplicationStart
public class Start extends Job {
	
	public void doJob() {
		
		// Context Path, para el despliegue de varias aplicaciones en Apache y no tener el problema del Path
		String ctxPath = FapProperties.get("fap.ctxPath");
		if (ctxPath != null){
			Play.ctxPath = ctxPath;
	        Router.load(Play.ctxPath);
		}
		
		loadLog4Config();
		
		if (Agente.count() == 0){
            Fixtures.delete();
            String agentesFile = "listas/initial-data/agentes.yml";
            Logger.info("Cargando agentes desde %s", agentesFile);
            play.test.Fixtures.loadModels(agentesFile);
        }
		
		// Para controlar el posible cambio de version del modulo fap de una aplicacion, y evitar el minimo daño posible en la BBDD
		// Ya que en versiones 1.2.X y anteriores la TableKeyValueDependency no existía, por lo que debemos controlar eso.
		boolean cambioVersion=true;
		
		if(TableKeyValue.count() == 0){
	        long count = TableKeyValue.loadFromFiles(false); //Carga las dos tablas, tanto la TableKeyValue como la TableKeyValueDependency, le pasamos false porque no se ha cargado nada (ningun .yaml) previamente
	        if (count > 0)
	        	Logger.info("Se cargaron desde fichero " + count + " registros de la tabla de tablas");
	        cambioVersion=false; // Si no existe la TableKeyValue, es que no es un cambio de versión, sino una inicializacion por primera vez de la BBDD
		}
		
		if(TableKeyValueDependency.count() == 0){
			// Si estamos en un cambio de version de la 1.2.X a la 2.X del modulo FAP
			if (cambioVersion) { // Sabemos que estamos en un cambio de version porque existe TableKeyValue, pero no existe TableKeyValueDependency
				Logger.info("Detectado un cambio de versión del módulo FAP que se venía utilizando, de la 1.2.X a la 2.X. Se procederá a reconfigurar las tablas de tablas");
				TableKeyValue.deleteAll(); // Borramos los posibles datos que hubiera de la TableKeyValue, ya que vamos a cargarlo todo otra vez y así evitamos la duplicidad de valores
				TableKeyValueDependency.deleteAll(); // Borramos la TableKeyValueDependency por manía, ya que no va a tener nada, supuestamente
				long count = TableKeyValue.loadFromFiles(false); // Cargamos todos los .yaml en las dos tablas anteriores que hemos borrado (limpiado)
				if (count > 0)
		        	Logger.info("Se cargaron desde fichero " + count + " registros de la tabla de tablas");
			}
			long count = TableKeyValueDependency.loadFromFiles(true); // Le pasamos true, porque la carga desde los .yaml, ya la hizo con el TableKeyValue
	        if (count > 0)
	        	Logger.info("Se cargaron desde fichero " + count + " registros de la tabla de tablas de Dependencias");
		}
		
		if (Mail.count() == 0){
			long count = Mails.loadFromFiles();
	        if (count > 0)
	        	Logger.info("Se cargaron desde fichero " + count + " registros de la tabla emails");
		}

		//Inicializa todas las relaciones a null de la solicitud
		if(FapProperties.getBoolean("fap.start.initSolicitud")){
			SolicitudGenerica generica = new SolicitudGenerica();
			List<SolicitudGenerica> list = generica.findAll();
			for (SolicitudGenerica solicitud : list) {
				solicitud.init();
				solicitud.save();
			}
		}
		//Inicializa o recupera el tipo de evaluacion
		TipoEvaluacion tipoEvaluacion = null;
		if((TipoEvaluacion.count() == 0) && (new File("conf/initial-data/tipoEvaluacion.json").exists())){
			tipoEvaluacion = BaremacionService.loadTipoEvaluacionFromJson("/conf/initial-data/tipoEvaluacion.json");
			tipoEvaluacion.save();
			Logger.info("Tipo de Baremación cargada correctamente desde fichero");
		}else if (!new File("conf/initial-data/tipoEvaluacion.json").exists()){
			Logger.info("No se puede leer el fichero que contiene los parámetros de la Evaluacion (/conf/initial-data/tipoEvaluacion.json)");
		} 
	}
	
	/**
	 * Carga la configuracion de log4j 
	 * a partir del fichero definido en "app.log.path"
	 * si la property existe y el fichero está definido
	 */
	private void loadLog4Config(){
		String log4jPropertyFile = FapProperties.get("app.log.path");
		if(log4jPropertyFile != null){
			URL resource = Play.classloader.getResource(log4jPropertyFile);
			if(resource != null){
				PropertyConfigurator.configure(resource);
			}
		}
	}
}
	
	
