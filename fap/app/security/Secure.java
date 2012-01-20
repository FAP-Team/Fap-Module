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

	public abstract boolean check(String id, String _permiso, String action, Map<String, Long> ids, Map<String, Object> vars);
	
	protected boolean nextCheck(String id, String _permiso, String action, Map<String, Long> ids, Map<String, Object> vars){
		boolean result = false;
		if(next != null){
			result = next.check(id, _permiso, action, ids, vars);
		}else{
			logger.error("[" + id + "] - nextCheck con next = null. Se asume que el permiso no se cumple.");
		}
		return result;
	}
	
	public static boolean checkIsEditableOrLess(String _permiso){
		return "editable".equals(_permiso) || "visible".equals(_permiso) || "none".equals(_permiso);
	}
	
	public static boolean checkIsVisibleOrLess(String _permiso){
		return "visible".equals(_permiso) || "none".equals(_permiso);
	}
	
	public static boolean checkIsNone(String _permiso){
		return "none".equals(_permiso);
	}
	
	/*
	 * Comprueba que el String action contiene uno de los 4 posibles valores válidos
	 */
	public static boolean checkAction(String action){
		return ("leer".equals(action) || "editar".equals(action) || "crear".equals(action) || "borrar".equals(action));
	}
}
