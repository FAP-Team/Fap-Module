package enumerado.gen;

public enum TipoFirmaJuridicaEnum{

	cif("cif"),
	representantes("representantes");

	private String valor;

	private TipoFirmaJuridicaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}