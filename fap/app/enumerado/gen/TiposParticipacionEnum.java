package enumerado.gen;

public enum TiposParticipacionEnum{

	creador("creador"),
	solicitante("solicitante"),
	representante("representante");

	private String valor;

	private TiposParticipacionEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}