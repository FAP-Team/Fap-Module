package enumerado.fap.gen;

public enum TiposParticipacionEnum{

	creador("Creador"),
	solicitante("Solicitante"),
	representante("Representante");

	private String valor;

	private TiposParticipacionEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}