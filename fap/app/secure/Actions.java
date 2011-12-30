package secure;

public enum Actions {
	
	LEER(),
	EDITAR(),
	CREAR(),
	BORRAR();

	/*
	 * Distintos nombres que pueden recibir cada una de las cuatro acciones definidas en FAP.
	 */
	
	public static Actions getAction(String action){
		action = action.toLowerCase();
		if ("read".equals(action) || "leer".equals(action) || "ver".equals(action))
			return LEER;
		if ("update".equals(action) || "edit".equals(action) || "modify".equals(action) || "editar".equals(action) || "modificar".equals(action) || "actualizar".equals(action))
			return EDITAR;
		if ("create".equals(action) || "crear".equals(action))
			return CREAR;
		if ("delete".equals(action) || "remove".equals(action) || "borrar".equals(action) || "eliminar".equals(action))
			return BORRAR;
		return null;
	}

}