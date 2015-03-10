package app;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.PropertyConfigurator;
import org.h2.constant.SysProperties;
import org.hibernate.ejb.EntityManagerImpl;
import org.joda.time.DateTime;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import config.InjectorConfig;
import controllers.AedController;
import emails.Mails;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;
import es.gobcan.certificados.Areas;
import es.gobcan.certificados.AreasFuncionales;
import es.gobcan.certificados.CertificadosResult;
import es.gobcan.certificados.Materias;
import es.gobcan.certificados.Tipos;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ArrayOfMensajeType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeCriteriaType;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import es.gobcan.certificados.Firmas;
import messages.Messages;
import models.*;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClassloader;
import play.data.binding.Binder;
import play.data.binding.ParamNode;
import play.data.binding.RootParamNode;
import play.data.binding.types.DateBinder;
import play.db.DB;
import play.db.Model;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Transactional;
import play.exceptions.YAMLException;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.guice.InjectSupport;
import play.mvc.Router;
import models.SolicitudGenerica;
import play.test.Fixtures;
import play.vfs.VirtualFile;
import properties.FapProperties;
import properties.Properties;
import services.BaremacionService;
import services.CertificadosService;
import services.CertificadosServiceException;
import services.ComunicacionesInternasService;
import services.ComunicacionesInternasServiceException;
import services.FirmaService;
import services.GestorDocumentalService;
import services.MensajeService;
import services.NotificacionService;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.PublicarService;
import services.RegistroLibroResolucionesService;
import services.RegistroService;
import services.SVDService;
import services.TercerosService;
import services.ServiciosGenericosService;
import services.TercerosService;
import services.MensajeService;
import services.genericos.ServiciosGenericosServiceImpl;
import services.VerificarDatosService;
import services.VerificarDatosServiceException;
import swhiperreg.ciservices.ArrayOfString;
import services.responses.PortafirmaCrearSolicitudResponse;
import utils.BaremacionUtils;
import utils.JsonUtils;
import utils.ModelUtils;
import config.InjectorConfig;
import emails.Mails;

@OnApplicationStart
public class Start extends Job {

	@Override
	public void doJob() {

		// Context Path, para el despliegue de varias aplicaciones en Apache y no tener el problema del Path
		String ctxPath = FapProperties.get("fap.ctxPath");
		if (ctxPath != null){
			Play.ctxPath = ctxPath;
	        Router.load(Play.ctxPath);
		}

		loadLog4Config();

		if (AdministracionFapJobs.count() == 0){
			AdministracionFapJobs jobs = new AdministracionFapJobs();
			jobs.save();
		}

		if (Agente.count() == 0){
            Fixtures.delete();
            String agentesFile = "listas/initial-data/agentes.yml";
            Logger.info("Cargando agentes desde %s", agentesFile);
            play.test.Fixtures.loadModels(agentesFile);
        }

		if (Consulta.count() == 0){
            Fixtures.delete();
            String consultasFile = "listas/initial-data/consultasSQL.yml";
            Logger.info("Cargando consultas desde %s", consultasFile);
            play.test.Fixtures.loadModels(consultasFile);
        }

		if (ParametroSVD.count() == 0 && ParametrosServicio.count() == 0){
            Fixtures.delete();
            String parametrosSVDFile = "listas/initial-data/parametrosSVD.yml";
            Logger.info("Cargando parámetros SVD desde %s", parametrosSVDFile);
            play.test.Fixtures.loadModels(parametrosSVDFile);
        }

		//Cargando mensajes de pagina desde conf/initial-data/paginas.yml

		if ((ConfigurarMensaje.count() == 0)){
			//Fap
			Fixtures.delete(ConfigurarMensaje.class);
			String paginasFile = "listas/initial-data/paginasMsj.yml";
			Logger.info("Cargando mensajes de páginas desde %s", paginasFile);
			play.test.Fixtures.loadModels(paginasFile);

			//Aplicacion
			paginasFile = "listas/initial-data/paginasAppMsj.yml";
			Logger.info("Cargando mensajes de páginas desde %s", paginasFile);
			play.test.Fixtures.loadModels(paginasFile);

			// Pagina de Login
			ConfigurarMensaje cm = new ConfigurarMensaje();
			cm.nombrePagina = "login";
			cm.formulario = "Login";
			cm.habilitar = false;
			cm.save();
		}else{
		//Siempre revisa que las páginas no hayan sido previamente cargadas -> Añade nuevas
			String paginasFileFap = "listas/initial-data/paginasMsj.yml";
			String paginasFileApp = "listas/initial-data/paginasAppMsj.yml";

			utils.Fixtures.loadConfigurarMensaje(paginasFileFap);
			utils.Fixtures.loadConfigurarMensaje(paginasFileApp);
		}

		// Para controlar el posible cambio de version del modulo fap de una aplicacion, y evitar el minimo daño posible en la BBDD
		// Ya que en versiones 1.2.X y anteriores la TableKeyValueDependency no existía, por lo que debemos controlar eso.
		boolean cambioVersion=true;

		// Si TableKeyValue no está vacía y tiene el atributo noVisible a NULL,
		// se lo ponemos a false (si no, salta un error)
		if (TableKeyValue.count() != 0) {
			Query query = JPA.em().createQuery("update TableKeyValue tablekeyvalue set o=false where o=null");
			query.executeUpdate();
		}

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

		// Inicializamos, recuperamos o actualizamos la Baremación
		BaremacionUtils.actualizarTipoEvaluacion();

		actualizarSemillaExpediente();

		if ((TramitesVerificables.count() == 0) && (Tramite.count() > 0)){
			List<Tramite> tramites = Tramite.findAll();
			ModelUtils.actualizarTramitesVerificables(tramites);
		}

		// Para mostrar información acerca de la inyección de los servicios
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		gestorDocumentalService.mostrarInfoInyeccion();

		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		firmaService.mostrarInfoInyeccion();

		RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);
		registroService.mostrarInfoInyeccion();

		NotificacionService notificacionService = InjectorConfig.getInjector().getInstance(NotificacionService.class);
		notificacionService.mostrarInfoInyeccion();

		PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
		portafirmaService.mostrarInfoInyeccion();

		PublicarService publicarService = InjectorConfig.getInjector().getInstance(PublicarService.class);
		publicarService.mostrarInfoInyeccion();

		RegistroLibroResolucionesService registroLibroResolucionesService = InjectorConfig.getInjector().getInstance(RegistroLibroResolucionesService.class);
		registroLibroResolucionesService.mostrarInfoInyeccion();

		TercerosService tercerosService = InjectorConfig.getInjector().getInstance(TercerosService.class);
		tercerosService.mostrarInfoInyeccion();

		MensajeService mensajeService = InjectorConfig.getInjector().getInstance(MensajeService.class);
		mensajeService.mostrarInfoInyeccion();
		
		ServiciosGenericosService serviciosGenericos = InjectorConfig.getInjector().getInstance(ServiciosGenericosService.class);
		serviciosGenericos.mostrarInfoInyeccion();

		ComunicacionesInternasService comunicacionInternaService = InjectorConfig.getInjector().getInstance(ComunicacionesInternasService.class);
		comunicacionInternaService.mostrarInfoInyeccion();
		
		VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
		verificarDatosService.mostrarInfoInyeccion();

		CertificadosService certificadosService = InjectorConfig.getInjector().getInstance(CertificadosService.class);
		certificadosService.mostrarInfoInyeccion();

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);
		svdService.mostrarInfoInyeccion();


		List<Class> assignableClasses = Play.classloader.getAssignableClasses(SolicitudGenerica.class);
        if(assignableClasses.size() > 1){
        	play.Logger.warn("¡¡ CUIDADO !! : Existen varias clases ("+assignableClasses.size()+") que extienden de SolicitudGenerica, esto creará conflictos GRAVES.");
        }

		Convocatoria convocatoria = Convocatoria.find("select convocatoria from Convocatoria convocatoria").first();
		if (convocatoria != null) {
			if (convocatoria.expedienteAed == null) {
				convocatoria.expedienteAed = new ExpedienteAed();
				convocatoria.save();
			}
		}

		// TimeZone por defecto del servidor
		play.Logger.info("TimeZone por defecto: "+TimeZone.getDefault());

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

	/**
	 * Actualiza la semilla del Expediente, en caso necesario,
	 * para que funcione la versión 1.3.2 de FAP y posteriores.
	 */
	private void actualizarSemillaExpediente() {

		Long size = (long) SemillaExpediente.findAll().size();
		Long idSemilla;
		Long valueSemilla;
		if (size == 1) {
			SemillaExpediente semilla = SemillaExpediente.find("select semillaExpediente from SemillaExpediente semillaExpediente").first();
			valueSemilla = semilla.semilla;
			idSemilla = semilla.id;

			if (valueSemilla != null){
				play.Logger.info("Semilla a buscar: "+ valueSemilla + ", encontrada: " + idSemilla);
				while (idSemilla < valueSemilla) {
					SemillaExpediente sem = new SemillaExpediente();
					sem.save();

					idSemilla = sem.id;
				}
				play.Logger.info("Semilla actualizada a " + idSemilla);
			}
		}
	}
}


