package app;

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

@OnApplicationStart
public class Start extends Job {
	
	public void doJob() {
		
		// Context Path, para el despliegue de varias aplicaciones en Apache y no tener el problema del Path
		String ctxPath = FapProperties.get("fap.ctxPath");
		if (ctxPath != null){
			Play.ctxPath = ctxPath;
	        Router.load(Play.ctxPath);
		}
		
		//Carga la configuracion de log4j
		String log4jPropertyFile = FapProperties.get("app.log.path");
		PropertyConfigurator.configure(Play.classloader.getResource(log4jPropertyFile));
		
		if (/*Play.mode.isDev() && */Agente.count() == 0){
            Fixtures.delete();
            String agentesFile = "listas/initial-data/agentes.yml";
            Logger.info("Cargando agentes desde %s", agentesFile);
            play.test.Fixtures.loadModels(agentesFile);
        }
		
		if(/*Play.mode.isDev() && */TableKeyValue.count() == 0){
	        long count = TableKeyValue.loadFromFiles();
	        Logger.info("Se cargaron desde fichero " + count + " registros de la tabla de tablas");
		}
		
		if (/*Play.mode.isDev() && */Mail.count() == 0){
			long count = Mails.loadFromFiles();
			Logger.info("Se cargaron desde fichero " + count + " registros de la tabla emails");
		}

		SolicitudGenerica generica = new SolicitudGenerica();
		List<SolicitudGenerica> list = generica.findAll();
		for (SolicitudGenerica solicitud : list) {
			solicitud.init();
			solicitud.save();
		}
		
	}
}
	
	
