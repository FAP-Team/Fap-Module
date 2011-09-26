package generator.utils;

import java.util.HashMap;
import java.util.Iterator;

public class TagParameters extends HashMap<String, String> {
	
	public void putStr(String key, String value){
		put(key, "'${value}'");
	}
	
	public void putList(String key, List lista){
		String out = "";
		Iterator<String> iterador = lista.iterator()
		while(iterador.hasNext()){
			out += "'${iterador.next()}'"
			if(iterador.hasNext()){
				out += ", "
			}
		}
		put(key, '[' + out + ']');
	}
	
	public String lista(){
		String out = "";
		
		Iterator<String> iterador = keySet().iterator()
		while(iterador.hasNext()){
			String key = iterador.next();
			String value = get(key);
			
			out += "$key:$value"
			if(iterador.hasNext()){
				out += ", "
			}
		}
		return out;
	}
}
