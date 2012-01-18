package properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;
import play.Play;

public class Properties extends play.utils.Properties {
	
	private static final Pattern environmentPropertyPattern = Pattern.compile("^%([a-zA-Z0-9_\\-]+)\\.(.*)$");
	
	public void load(InputStream input, boolean overwrite) throws IOException{
		Properties p = new Properties();
		p.load(input);
		
		for(String key : p.keySet()){
			put(key, p.get(key), overwrite);
		}
	}
	
	public void load(java.util.Properties p, boolean overwrite){
		Enumeration<Object> keys = p.keys();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			put(key, (String)p.get(key), overwrite);
		}
	}
		
	public void copy(java.util.Properties to, boolean overwrite){
		for(java.util.Map.Entry<String, String> entry : entrySet()){
			if(overwrite || !to.contains(entry.getKey())){
				to.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void put(String key, String value, boolean overwrite){
		if(!containsKey(key) || overwrite){
			put(key, value);
		}
	}
	
	/**
	 * Aplica las properties del entorno en el que se está ejecutando la aplicación
	 */
	public void applyEnvironment(){
		applyEnvironment(Play.id);
	}
	
	
	/**
	 * Añade las properties de entorno a la lista
	 * 
	 * %entorno.property.name
	 * 
	 * En el caso de que el parámetro evn coincida con el inicio de la property
	 * se añade la property a la lista
	 * 
	 * env = %prod
	 * %prod.http.path = /home
	 * => 
	 * http.path = /home
	 * 
	 * @param env
	 */
	public void applyEnvironment(String env){
		//Carga las properties del modulo en configuration
		Set<String> keySet = new HashSet<String>(keySet());
		for (String key : keySet) {
			String value = get(key).trim();
			
			//Comprueba si es una property de entorno que se aplica
			Matcher matcher = environmentPropertyPattern.matcher(key);
			if (matcher.matches()) {
				String instance = matcher.group(1);
				if (instance.equals(env)) {
					put(matcher.group(2), value, false);
				}
			}
		}		
	}
	
}
