package enumerado.gen;

public enum TipoConsultaEnum{

	tipoSQL("tipoSQL"),
	tipoJPQL("tipoJPQL");

	private String valor;

	private TipoConsultaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}