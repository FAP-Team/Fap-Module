package enumerado.fap.gen;

public enum RolesEnum{

	administrador("Administrador"),
	usuario("Usuario"),
	gestor("Gestor"),
	revisor("Revisor");

	private String valor;

	private RolesEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}