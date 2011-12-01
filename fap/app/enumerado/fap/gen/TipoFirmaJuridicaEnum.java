package enumerado.fap.gen;

public enum TipoFirmaJuridicaEnum{

	cif("Certificado de empresa"),
	representantes("Certificados de los representantes");

	private String valor;

	private TipoFirmaJuridicaEnum(String valor){
		this.valor = valor;
	}

	@Override
	public String toString() {
		return this.valor;
	}
}