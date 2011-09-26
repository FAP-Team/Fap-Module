package tags;

import java.util.*;

/**
 * 
 * Clase que permite añadir para parametros
 * y devuelve una representación en json de los mismos
 *
 */
public class JsonParameters {
	
	private List<String> parameters;
	
	public JsonParameters() {
		parameters = new ArrayList<String>();
	}
	
	public void put(String key, Object value){
		parameters.add(key + ":" + value);
	}
	
	public void putStr(String key, Object value){
		put(key, "'" + value + "'");
	}
	
	public String getMap(){
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		Iterator<String> iterator = parameters.iterator();
		while(iterator.hasNext()){
			sb.append(iterator.next());
			if(iterator.hasNext())
				sb.append(",");
		}
		
		sb.append("}");
		return sb.toString();
	}
}
