package enumerado.fap.gen;

public enum TipoDePersonaEnum{

	fisica("Persona física"),
	juridica("Persona jurídica");

	private String valor;

	private TipoDePersonaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}