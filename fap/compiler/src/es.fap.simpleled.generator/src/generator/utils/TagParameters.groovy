package generator.utils;

import java.util.HashMap;
import java.util.Iterator;

public class TagParameters extends HashMap<String, String> {
	
	public void putStr(String key, String value){
		value = "'${value}'";
		put(key, value);
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
	
	/*
	 * Si el parámetro variasLineas es true, cada parámetro de la lista será escrito en una nueva linea.
	 */
	public String lista(boolean variasLineas = false){
		String nuevaLinea = "";
		if (variasLineas) nuevaLinea = "\n     ";
		String out = "";
		Iterator<String> iterador = keySet().sort().iterator()
		while(iterador.hasNext()){
			String key = iterador.next();
			String value = get(key);
			out += "$nuevaLinea$key:$value"
			if(iterador.hasNext()){
				out += ", "
			}
		}
		return out;
	}
}
