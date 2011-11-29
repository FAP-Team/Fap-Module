package enumerado.gen;

public enum EstadosSolicitudEnum{

	borrador("borrador"),
	iniciada("iniciada");

	private String valor;

	private EstadosSolicitudEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}