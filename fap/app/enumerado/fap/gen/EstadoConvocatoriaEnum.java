package enumerado.fap.gen;

public enum EstadoConvocatoriaEnum{

	presentacion("Presentación"),
	instruccion("Instrucción");

	private String valor;

	private EstadoConvocatoriaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}