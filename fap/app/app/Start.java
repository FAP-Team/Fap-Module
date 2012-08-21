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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.PropertyConfigurator;
import org.h2.constant.SysProperties;
import org.hibernate.ejb.EntityManagerImpl;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import config.InjectorConfig;

import emails.Mails;

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
import services.FirmaService;
import services.GestorDocumentalService;
import services.NotificacionService;
import services.RegistroService;
import utils.BaremacionUtils;
import utils.JsonUtils;

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
			
			// Para la pagina de Login
			ConfigurarMensaje cm = new ConfigurarMensaje();
			cm.nombrePagina = "login";
			cm.formulario = "login";
			cm.habilitar = false;
			cm.save();
		}else{


		//Siempre revisa que las páginas no hayan sido previamente cargadas -> Añade nuevas
			String paginasFileFap = "listas/initial-data/paginasMsj.yml";
			String paginasFileApp = "listas/initial-data/paginasAppMsj.yml";
//			String paginasFileFapLista = "listas/initial-data/paginasMsjLista.yml";
//			String paginasFileAppLista = "listas/initial-data/paginasAppMsjLista.yml";
            
            loadConfigurarMensaje(paginasFileFap);
            loadConfigurarMensaje(paginasFileApp);
            
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
		
		// Para mostrar información acerca de la inyección de los servicios
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		gestorDocumentalService.mostrarInfoInyeccion();
		
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		firmaService.mostrarInfoInyeccion();
		
		RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);
		registroService.mostrarInfoInyeccion();
		
		NotificacionService notificacionService = InjectorConfig.getInjector().getInstance(NotificacionService.class);
		notificacionService.mostrarInfoInyeccion();
	}

	@Transactional
	private void loadConfigurarMensaje(String paginasFile) {
		Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");
		Object o = play.test.Fixtures.loadYaml(paginasFile);

		if (o instanceof LinkedHashMap<?, ?>) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;
			for (Object key : objects.keySet()) {
				System.out.println("key: " + key);
				Matcher matcher = keyPattern.matcher(key.toString().trim());
				if (matcher.matches()) {
					// Type of the object. i.e. models.employee
					String type = matcher.group(1);
					// Id of the entity i.e. nicolas
					String id = matcher.group(2);
					System.out.println("id: " + id + "   type: " + type);

					if (!type.startsWith("models.")) {
						type = "models." + type;
					}

					// Was the entity already defined?

					// Those are the properties that were parsed from the YML
					// file
					final Map<?, ?> entityValues = objects.get(key);

					// Prefix is object, why is that?
					final Map<String, String[]> fields = serialize(
							entityValues, "object");

					try {

						@SuppressWarnings("unchecked")
						Class<Model> cType = (Class<Model>) Play.classloader
								.loadClass(type);
						final Map<String, String[]> resolvedFields = resolveDependencies(
								cType, fields, (Map<String, Object>) o);

						RootParamNode rootParamNode = ParamNode
								.convert(resolvedFields);
						// This is kind of hacky. This basically says that if we
						// have an embedded class we should ignore it.
						if (Model.class.isAssignableFrom(cType)) {

							Model model = (Model) Binder.bind(rootParamNode,
									"object", cType, cType, null);
							for (Field f : model.getClass().getFields()) {
								if (f.getType().isAssignableFrom(Map.class)) {
									f.set(model,
											objects.get(key).get(f.getName()));
								}
								if (f.getType().equals(byte[].class)) {
									f.set(model,
											objects.get(key).get(f.getName()));
								}
							}

							// En este momento tengo el ConfigurarMensaje
							// completo y compruebo que no esté en BBDD
							// Para guardarlo
							// TODO
							model._save();

						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
			
			
			//Intento de lectura desde yaml a estructura de datos
			//http://forums.bukkit.org/threads/constructing-an-instance-of-a-class-with-snakeyaml.4153/
			//LinkedHashMap<String, ConfigurarMensaje> paginasFap = (LinkedHashMap)play.test.Fixtures.loadYaml(paginasFileFap);
			//LinkedHashMap<String, ConfigurarMensaje> paginasApp = (LinkedHashMap)play.test.Fixtures.loadYaml(paginasFileApp);
			//HashMap<String, ConfigurarMensaje> obj = new HashMap<String, ConfigurarMensaje>();
			//Yaml y = new Yaml(new CustomClassLoaderConstructor(ConfigurarMensaje.class.getClassLoader()));
		    //   try {
		    //       obj = (HashMap<String, ConfigurarMensaje>) y.load(new FileReader(paginasFileFap));
		    //   } catch (FileNotFoundException e) {
		    //       e.printStackTrace();
		    //   }
			//Lista<String> donde tengo todos los parámetros obtenidos de bbdd
			//Collection<ConfigurarMensaje> valoresCM = (Collection<ConfigurarMensaje>)paginasFap2.values();
			
			//Cargo la lista de paginas
//			List<String> listaPagApp = (List<String>)play.test.Fixtures.loadYaml(paginasFileAppLista);
//			List<String> listaPagFap = (List<String>)play.test.Fixtures.loadYaml(paginasFileFapLista);
//			
//			
//			List<String> listaTotal = new ArrayList<String>();
//			listaTotal.addAll(listaPagFap);
//			listaTotal.addAll(listaPagApp);
//			
//			//Obtengo las paginas que ya están almacenadas
//			List<ConfigurarMensaje> paginasMensaje = ConfigurarMensaje.findAll();
			
			//Compruebo si la página existe.
//			boolean existe = false;
//			for (String str : listaTotal){
//				 existe = false;
//				 for (ConfigurarMensaje cm : paginasMensaje) {
//					if(str.equals(cm.nombrePagina)){
//						existe = true; //Encontrado
//						break;
//					}
//				}
//				if (!existe){ //Si no lo encontré -> Crear
//					ConfigurarMensaje cm = new ConfigurarMensaje();
//					cm.nombrePagina = str;
//					cm.habilitar = false;
//					cm.save();
//					existe = true;
//				}
//			}
			
			 

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


	
	 @SuppressWarnings("unchecked")
	    /**
	     *  Resolve dependencies between objects using their keys. For each referenced objects, it sets the foreign key
	     */
	    static Map<String, String[]> resolveDependencies(Class<Model> type, Map<String, String[]> yml, Map<String, Object> idCache) {

	        // Contains all the fields (object properties) we should look up
	        final Set<Field> fields = new HashSet<Field>();
	        final Map<String, String[]> resolvedYml = new HashMap<String, String[]>();
	        resolvedYml.putAll(yml);

	        // Look up the super classes
	        Class<?> clazz = type;
	        while (!clazz.equals(Object.class)) {
	            Collections.addAll(fields, clazz.getDeclaredFields());
	            clazz = clazz.getSuperclass();
	        }


	        // Iterate through the Entity property list
	        // @Embedded are not managed by the JPA plugin
	        // This is not the nicest way of doing things.
	         //modelFields =  Model.Manager.factoryFor(type).listProperties();
	        final List<Model.Property> modelFields =  new JPAPlugin.JPAModelLoader(type).listProperties();

	        for (Model.Property field : modelFields) {
	            // If we have a relation, get the matching object
	            if (field.isRelation) {
	                // These are the Ids that were set in the yml file (i.e person(nicolas)-> nicolas is the id)
	                final String[] ids = resolvedYml.get("object." + field.name);
	                if (ids != null) {
	                    final String[] resolvedIds = new String[ids.length];
	                    for (int i = 0; i < ids.length; i++) {
	                        final String id = field.relationType.getName() + "-" + ids[i];
	                        if (!idCache.containsKey(id)) {
	                            throw new RuntimeException("No previous reference found for object of type " + field.name + " with key " + ids[i]);
	                        }
	                        // We now get the primary key
	                        resolvedIds[i] = idCache.get(id).toString();
	                    }
	                    // Set the primary keys instead of the object itself.
	                    // Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName() returns the primary key label.
	                    if (Model.class.isAssignableFrom(field.relationType )) {
	                        resolvedYml.put("object." + field.name + "." + Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName(), resolvedIds);
	                    } else {
	                        // Might be an embedded object
	                        final String id = field.relationType.getName() + "-" + ids[0];
	                        Object o = idCache.get(id);
	                        // This can be a composite key
	                        if (o.getClass().isArray()) {
	                            for (Object a : (Object[])o) {
	                                for (Field f : field.relationType.getDeclaredFields()) {
	                                    try {
	                                        resolvedYml.put("object." + field.name + "." + f.getName(), new String[] {f.get(a).toString()});
	                                    } catch(Exception e) {
	                                        // Ignores
	                                    }
	                                }
	                            }
	                        } else {
	                            for (Field f : field.relationType.getDeclaredFields()) {
	                                try {
	                                    resolvedYml.put("object." + field.name + "." + f.getName(), new String[] {f.get(o).toString()});
	                                } catch(Exception e) {
	                                    // Ignores
	                                }
	                            }
	                        }
	                    }
	                }

	                resolvedYml.remove("object." + field.name);
	            }
	        }
	        // Returns the map containing the ids to load for this object's relation.
	        return resolvedYml;
	    }
	
   static Map<String, String[]> serialize(Map<?, ?> entityProperties, String prefix) {

       if (entityProperties == null) {
           return MapUtils.EMPTY_MAP;
       }

       final Map<String, String[]> serialized = new HashMap<String, String[]>();

       for (Object key : entityProperties.keySet()) {

           Object value = entityProperties.get(key);
           if (value == null) {
               continue;
           }
           if (value instanceof Map<?, ?>) {
               serialized.putAll(serialize((Map<?, ?>) value, prefix + "." + key));
           } else if (value instanceof Date) {
               serialized.put(prefix + "." + key.toString(), new String[]{new SimpleDateFormat(DateBinder.ISO8601).format(((Date) value))});
           } else if (Collection.class.isAssignableFrom(value.getClass())) {
               Collection<?> l = (Collection<?>) value;
               String[] r = new String[l.size()];
               int i = 0;
               for (Object el : l) {
                   r[i++] = el.toString();
               }
               serialized.put(prefix + "." + key.toString(), r);
           } else if (value instanceof String && value.toString().matches("<<<\\s*\\{[^}]+}\\s*")) {
               Matcher m = Pattern.compile("<<<\\s*\\{([^}]+)}\\s*").matcher(value.toString());
               m.find();
               String file = m.group(1);
               VirtualFile f = Play.getVirtualFile(file);
               if (f != null && f.exists()) {
                   serialized.put(prefix + "." + key.toString(), new String[]{f.contentAsString()});
               }
           } else {
               serialized.put(prefix + "." + key.toString(), new String[]{value.toString()});
           }
       }

       return serialized;
   }
	
}
	
	
