package security;

import java.util.Map;

import org.apache.log4j.Logger;

abstract public class Secure {
	
	protected Logger logger = Logger.getLogger(Secure.class);
	
	protected Secure next;
	
	public Secure(Secure next){
		this.next = next;
	}
	
	public Secure getNext() {
		return next;
	}

	public void setNext(Secure next) {
		this.next = next;
	}

	public abstract boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars);
	
	protected boolean nextCheck(String id, String action, Map<String, Long> ids, Map<String, Object> vars){
		boolean result = false;
		if(next != null){
			result = next.check(id, action, ids, vars);
		}else{
			logger.error("[" + id + "] - nextCheck con next = null. Se asume que el permiso no se cumple.");
		}
		return result;
	}
	
	/**
	 * Método para mantener la compatibilidad con los nombres de las acciones.
	 * En los popups se utilizan nombres en español (leer, borrar, editar, crear) mientras
	 * que en los permisos se utilizan nombres en inglés (read, delete, update, create).
	 * 
	 * Este método traduce el nombre en español al inglés.
	 * 
	 * En futuras versiones debería adoptarse la nomenclatura inglés en todos sitios y este
	 * método no hara falta.
	 * 
	 * @param action
	 * @return
	 */
	public String transform(String action){
		String actionResult = action;
		if("leer".equals(action)) actionResult="read";
		else if("borrar".equals(action)) actionResult="delete";
		else if("editar".equals(action)) actionResult="edit";
		else if("crear".equals(action)) actionResult="create";
		return actionResult;
	}
}
