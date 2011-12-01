package enumerado.fap.gen;

public enum EstadosSolicitudEnum{

	borrador("Borrador"),
	iniciada("Iniciada");

	private String valor;

	private EstadosSolicitudEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}