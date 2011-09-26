package tags;

import java.util.HashMap;

public class JSMap extends HashMap<String, String> {

	public void putStr(String key, String value){
		put(key, "'" + value  + "'");
	}
	
}
