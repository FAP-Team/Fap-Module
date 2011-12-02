package enumerado.fap.gen;

public enum TipoConsultaEnum{

	tipoSQL("SQL"),
	tipoJPQL("JPSQL");

	private String valor;

	private TipoConsultaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}