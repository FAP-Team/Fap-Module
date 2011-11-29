package enumerado.gen;

public enum RolesEnum{

	administrador("administrador"),
	usuario("usuario");

	private String valor;

	private RolesEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}