package properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;
import play.Play;
import play.PlayPlugin;

public class FapProperties extends PlayPlugin {
	
	// Configuración
	private static final String FAP_APPCONFIGURATION = Play.modules.get("fap")
	.getRealFile() + "/conf/application.conf";
	private static long configurationlastModified = 0;
	private static Properties configuration;
	
	@Override
	public void onApplicationStart() {
		loadProperties();
    }
	
	public static Properties get() {
		if (Play.mode.isDev()) {   //solo se ejecuta en desarrollo.
			loadProperties();
		}
		return configuration;
	}

	public static String get(String key) {
		if (Play.mode.isDev()) {   //solo se ejecuta en desarrollo.
			loadProperties();
		}
		return configuration.get(key);
	}

	public static Boolean getBoolean(String key) {
		if (Play.mode.isDev()) {   //solo se ejecuta en desarrollo.
			loadProperties();
		}
		return configuration.getBoolean(key);
	}

	public static int getInt(String key) {
		if (Play.mode.isDev()) {   //solo se ejecuta en desarrollo.
			loadProperties();
		}
		return configuration.getInt(key);
	}

	public static long getLong(String key) {
		if (Play.mode.isDev()) {   //solo se ejecuta en desarrollo.
			loadProperties();
		}
		return configuration.getLong(key);
	}
	
	public static void loadProperties () {
		File file = new File(FAP_APPCONFIGURATION);

		if (configuration == null || configurationlastModified < file.lastModified()) {
			configuration = new Properties();
			configuration.load(Play.configuration, true);
			configurationlastModified = file.lastModified();

			Properties fapConf = new Properties();

			try {
				fapConf.load(new FileInputStream(file), false);

				Pattern pattern = Pattern.compile("^%([a-zA-Z0-9_\\-]+)\\.(.*)$");
				for (Object key : fapConf.keySet()) {
					Matcher matcher = pattern.matcher(key + "");
					if (matcher.matches()) {
						String instance = matcher.group(1);
						if (instance.equals(Play.id)) {
							configuration.put(matcher.group(2), fapConf.get(key).toString().trim(), false);
						}
					}
					else {
						configuration.put((String) key, fapConf.get(key).toString().trim(), false);						
					}
				}

				// Carga las properties del modulo en la aplicacion. 
				for (Object key : configuration.keySet()) {
					if (!Play.configuration.containsKey(key)) {
						Play.configuration.put(key, configuration.get(key).toString());
					}
				}
			} catch (FileNotFoundException e) {
				Logger.error(e, "File not found %s", FAP_APPCONFIGURATION);
			} catch (IOException e) {
				Logger.error(e, "Excepción %s", FAP_APPCONFIGURATION);
			}
		}
	}	
}
