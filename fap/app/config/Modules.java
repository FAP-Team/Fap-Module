package config;

import play.Play;
import java.lang.Boolean;

public class Modules {
	public static boolean getProperty(String property){
		if(Play.configuration.getProperty(property) != null){
			return Boolean.parseBoolean(Play.configuration.getProperty(property));
		}
		return false;
	}
}
