package enumerado.gen;

public enum EstadosSolicitudEnum{

	borrador("borrador"),
	iniciada("iniciada"),
	verificado("verificado");

	private String valor;

	private EstadosSolicitudEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}