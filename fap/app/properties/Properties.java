package properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;
import play.Play;

public class Properties extends play.utils.Properties {
	
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
	
	public void put(String key, String value, boolean overwrite){
		if(!containsKey(key) || overwrite){
			put(key, value);
		}
	}
}
