package properties;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class MapPropertyPlaceholder implements PropertyPlaceholder {

	private static Logger logger = Logger.getLogger(MapPropertyPlaceholder.class);
	
	private Map<String, String> map;
	
	public MapPropertyPlaceholder(){
		map = new HashMap<String, String>();
	}
	
	public MapPropertyPlaceholder(Map<String, String> map){
		this.map = map;
	}
	
	public MapPropertyPlaceholder(String ... props){
		if(props.length % 2 != 0){
			throw new IllegalArgumentException("La lista de properties debe ser múltiple de 2");
		}
		
		map = new HashMap<String, String>();
		for(int i = 0; i < props.length; i = i+2){
			put(props[i], props[i + 1]);
		}
	}
	
	public void put(String key, String value){
		map.put(key, value);
	}
	
	@Override
	public String get(String key) {
		String ret = null;
		if(!map.containsKey(key)){
			logger.error("Property " + key + " not found");
		}else{
			ret = map.get(key);
		}
		return ret;
	}

	@Override
	public Boolean getBoolean(String key) {
		String stringValue = get(key);
		Boolean ret = null;
		if(stringValue != null){
			ret = Boolean.parseBoolean(stringValue);
		}
		return ret;
	}

	@Override
	public int getInt(String key) {
		String stringValue = get(key);
		int ret = 0;
		if(stringValue != null){
			try {
				ret = Integer.parseInt(stringValue);
			}catch(NumberFormatException e){
				logger.error("Property " + key + " not integer format");
			}
		}
		return ret;
	}

	@Override
	public long getLong(String key) {
		String stringValue = get(key);
		long ret = 0;
		if(stringValue != null){
			try {
				ret = Long.parseLong(stringValue);
			}catch(NumberFormatException e){
				logger.error("Property " + key + " not integer format");
			}
		}
		return ret;
	}

}
