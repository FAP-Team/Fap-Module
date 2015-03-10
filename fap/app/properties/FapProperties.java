package properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import play.Logger;
import play.Play;
import play.PlayPlugin;

public class FapProperties extends PlayPlugin {

	// Configuración
	private static long configurationlastModified = 0;
	private static Properties configuration;

	@Override
	public void onApplicationStart() {
		loadProperties();
    }

	/** 
	 * En modo desarrollo carga las propiedades si es necesario en cada peticion.
	 */
	@Override
	public void beforeInvocation() {
		if(Play.mode.isDev()){
			loadProperties();
		}
	}
	
	public static Properties get() {
		return configuration;
	}

	public static String get(String key) {		
		return configuration.get(key);
	}

	public static Boolean getBoolean(String key) {
		return configuration.getBoolean(key);
	}

	public static int getInt(String key) {
		return configuration.getInt(key);
	}

	public static long getLong(String key) {
		return configuration.getLong(key);
	}
	
	public static void setBoolean(String key, boolean bool) {
	    configuration.put(key, bool ? "true" : "false");
	}
	
	/**
	 * Comprueba si se está ejecutando una aplicacion
	 * o el módulo directamente (por ejemplo ejecutando los test del módulo)
	 * @return
	 */
	public static boolean isApplication(){
		return Play.modules.containsKey("fap");
	}
	
	public static void loadProperties () {
		if(isApplication()){
			File moduleConfFile = getModuleConfFile();
			long lastMod = moduleConfFile.lastModified();
			if (configuration == null || configurationlastModified < lastMod) {
				configurationlastModified = lastMod;
				configuration = new Properties();
				try {
					configuration.load(new FileInputStream(moduleConfFile), false);
					configuration.applyEnvironment();
					configuration.load(Play.configuration, true);
					configuration.copy(Play.configuration, false);
				} catch (FileNotFoundException e) {
					Logger.error(e, "File not found %s", moduleConfFile.getAbsolutePath());
				} catch (IOException e) {
					Logger.error(e, "Excepción al leer el fichero %s", moduleConfFile.getAbsolutePath());
				}
			}
		}else{
			//Es el módulo, seguramente se esten ejecutando los test
			configuration = new Properties();
			configuration.load(Play.configuration, true);
		}
	}

    public static void updateProperties(java.util.Properties properties) {
        configuration.load(properties, true);
    }

    public static void updateProperty(String key, String value) {
        java.util.Properties properties = new java.util.Properties();
        properties.put(key,value);
        updateProperties(properties);
    }

	private static File getModuleConfFile() {
		String moduleConfPath = Play.modules.get("fap").getRealFile() + "/conf/application.conf";
		File moduleConfFile = new File(moduleConfPath);
		return moduleConfFile;
	}
}
