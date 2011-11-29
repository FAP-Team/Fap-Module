package enumerado.gen;

public enum TipoDePersonaEnum{

	fisica("fisica"),
	juridica("juridica");

	private String valor;

	private TipoDePersonaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}