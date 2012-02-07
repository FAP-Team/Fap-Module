package security;

public enum Grafico {

	Oculto(){
		public String toString(){
			return "oculto";
		}
	},
	
	Visible(){
		public String toString(){
			return "visible";
		}
	},
	
	Editable(){
		public String toString(){
			return "editable";
		}
	};
	
	/*
	 * Comprueba jerárquicamente un permiso gráfico.
	 * La jerarquía es: Editable > Visible > Oculto
	 */
	public boolean check(Grafico grafico){
		if (grafico == null) return false;
		if (equals(Editable)) return true;
		if (equals(Visible)) return grafico.isVisibleOrLess();
		return grafico.isOculto();
	}
	
	public boolean isEditableOrLess(){
		return equals(Editable) || equals(Visible) || equals(Oculto);
	}
	
	public boolean isVisibleOrLess(){
		return equals(Visible) || equals(Oculto);
	}
	
	public boolean isVisibleOrGreater(){
		return equals(Editable) || equals(Visible);
	}
	
	public boolean isEditable(){
		return equals(Editable);
	}
	
	public boolean isOculto(){
		return equals(Oculto);
	}
	
	public static Grafico parse(String grafico){
		if ("oculto".equals(grafico)) return Oculto;
		if ("visible".equals(grafico)) return Visible;
		if ("editable".equals(grafico)) return Editable;
		return null;
	}
	
}
