package enumerado.gen;

public enum TiposRepresentantesEnum{

	mancomunado("mancomunado"),
	solidario("solidario"),
	administradorUnico("administradorUnico");

	private String valor;

	private TiposRepresentantesEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}