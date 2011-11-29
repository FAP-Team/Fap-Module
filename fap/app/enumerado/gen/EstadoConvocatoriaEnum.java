package enumerado.gen;

public enum EstadoConvocatoriaEnum{

	presentacion("presentacion"),
	instruccion("instruccion");

	private String valor;

	private EstadoConvocatoriaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}