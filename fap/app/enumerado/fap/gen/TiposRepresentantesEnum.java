package enumerado.fap.gen;

public enum TiposRepresentantesEnum{

	mancomunado("Mancomunado"),
	solidario("Solidario"),
	administradorUnico("Administrador Único");

	private String valor;

	private TiposRepresentantesEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}