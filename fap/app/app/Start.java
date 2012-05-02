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
		
		/* ESTO CREO QUE YA NO HARIA FALTA  ¿? */
		// Context Path, para el despliegue de varias aplicaciones en Apache y no tener el problema del Path
		String ctxPath = FapProperties.get("fap.ctxPath");
		if (ctxPath != null){
			Play.ctxPath = ctxPath;
	        Router.load(Play.ctxPath);
		}
		/* ---------------------------------- */
		
		//Carga la configuracion de log4j
		String log4jPropertyFile = FapProperties.get("app.log.path");
		PropertyConfigurator.configure(Play.classloader.getResource(log4jPropertyFile));
		
		if (Agente.count() == 0){
            Fixtures.delete();
            String agentesFile = "listas/initial-data/agentes.yml";
            Logger.info("Cargando agentes desde %s", agentesFile);
            play.test.Fixtures.loadModels(agentesFile);
        }
		
		if(TableKeyValue.count() == 0){
	        long count = TableKeyValue.loadFromFiles();
	        Logger.info("Se cargaron desde fichero " + count + " registros de la tabla de tablas");
		}
		
		if (Mail.count() == 0){
			long count = Mails.loadFromFiles();
			Logger.info("Se cargaron desde fichero " + count + " registros de la tabla emails");
		}

		SolicitudGenerica generica = new SolicitudGenerica();
		List<SolicitudGenerica> list = generica.findAll();
		for (SolicitudGenerica solicitud : list) {
			solicitud.init();
			solicitud.save();
		}
		
		actualizarSemillaExpediente();
		
	}
	
	/**
	 * Actualiza la semilla del Expediente, en caso necesario,
	 * para que funcione la versión 1.3.2 de FAP y posteriores.
	 */
	private void actualizarSemillaExpediente () {
		Long size = (long) SemillaExpediente.findAll().size();
		Long idSemilla;
		Long valueSemilla;
		if (size == 1) {
			SemillaExpediente semilla = SemillaExpediente.find("select semillaExpediente from SemillaExpediente semillaExpediente").first();
			valueSemilla = semilla.semilla;
			if (valueSemilla != null) {
				idSemilla = semilla.id;
			
				play.Logger.info("Semilla a buscar: "+valueSemilla+", encontrada: "+idSemilla);
			
				while (idSemilla < valueSemilla) {
					SemillaExpediente sem = new SemillaExpediente();
					sem.save();
				
					idSemilla = sem.id;
				}
				play.Logger.info("Semilla actualizada a "+idSemilla);
			}
		}
	}
}
	
	
