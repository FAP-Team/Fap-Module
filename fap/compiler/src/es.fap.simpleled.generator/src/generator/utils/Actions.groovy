package generator.utils;

import es.fap.simpleled.led.PaginaAccion;

public enum Actions {
	
	LEER(),
	EDITAR(),
	CREAR(),
	BORRAR();
	
	public static Actions getAction(String action){
		action = action.toLowerCase();
		if ("read".equals(action) || "leer".equals(action) || "ver".equals(action))
			return LEER;
		if ("edit".equals(action) || "modify".equals(action) || "editar".equals(action) || "modificar".equals(action))
			return EDITAR;
		if ("create".equals(action) || "crear".equals(action))
			return CREAR;
		if ("delete".equals(action) || "remove".equals(action) || "borrar".equals(action) || "eliminar".equals(action))
			return BORRAR;
		return null;
	}
	
	public static String getAccion(PaginaAccion paginaAccion){
		return getAccion((String)paginaAccion?.accion);
	}
	
	public static String getAccion(String accion){
		if (accion != null)
			return accion;
		return "editar";
	}

}