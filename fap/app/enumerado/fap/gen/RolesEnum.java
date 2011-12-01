package enumerado.fap.gen;

public enum RolesEnum{

	administrador("Administrador"),
	usuario("Usuario");

	private String valor;

	private RolesEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}