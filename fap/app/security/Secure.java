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

	
	public abstract ResultadoPermiso check(String id, String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars);
	
	public abstract ResultadoPermiso accion(String id, Map<String, Long> ids, Map<String, Object> vars);
	
	
	public boolean checkGrafico(String id, String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars){
		if (accion.equals("borrado")) return true;
		ResultadoPermiso resultado = check(id, grafico, accion, ids, vars);
		if (resultado == null)
			return false;
		boolean result =resultado.checkGrafico(grafico);
		return result;
	}
	
	public boolean checkAcceso(String id, String accion, Map<String, Long> ids, Map<String, Object> vars){
		if (accion.equals("borrado")) return true;
		ResultadoPermiso resultado = check(id, "visible", accion, ids, vars);
		if (resultado == null) return false;
		return resultado.checkAcceso(accion);
	}
	
	public String getPrimeraAccion(String id, Map<String, Long> ids, Map<String, Object> vars){
		ResultadoPermiso resultado = accion(id, ids, vars);
		if (resultado == null) return "editar";
		return resultado.getPrimeraAccion();
	}
	
	protected ResultadoPermiso nextCheck(String id, String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars){
		if(next != null)
			return next.check(id, grafico, accion, ids, vars);
		logger.error("[" + id + "] - nextCheck con next = null. Se asume que el permiso no se cumple.");
		return null;
	}
	
	protected ResultadoPermiso nextAccion(String id, Map<String, Long> ids, Map<String, Object> vars){
		if(next != null)
			return next.accion(id, ids, vars);
		logger.error("[" + id + "] - nextAccion con next = null. Se asume que el permiso no se cumple.");
		return null;
	}
	
}
