package security;

public enum Accion {
	
	Leer(){
		public String toString(){
			return "leer";
		}
	}, 
	
	Editar(){
		public String toString(){
			return "editar";
		}	
	},
	
	Crear(){
		public String toString(){
			return "crear";
		}
	},
	
	Duplicar(){
		public String toString(){
			return "duplicar";
		}
	},
	
	Borrar(){
		public String toString(){
			return "borrar";
		}
	},
	
	Borrado(){
		public String toString(){
			return "borrado";
		}
	},
	
	All(), Denegar();
	
	public static Accion parse(String accion){
		if ("leer".equals(accion)) return Leer;
		if ("editar".equals(accion)) return Editar;
		if ("crear".equals(accion)) return Crear;
		if ("borrar".equals(accion)) return Borrar;
		if ("borrado".equals(accion)) return Borrado;
		if ("duplicar".equals(accion)) return Duplicar;
		return null;
	}
	
}
