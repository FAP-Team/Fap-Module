package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;

public class TextoValidator extends LedElementValidator{

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		String simple = atributo.getType().getSimple();
		String special = atributo.getType().getSpecial();
		return (
			"String".equals(simple) ||
			"Long".equals(simple) ||
			"Integer".equals(simple) ||
			"Double".equals(simple) ||
			"LongText".equals(simple) ||
			"Telefono".equals(special) ||
			"Email".equals(special) ||
			"Moneda".equals(special) ||
			"Cif".equals(special)
		);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de alguno de los siguientes tipos: String, LongText, Integer, Double, LongText, Telefono, Email, Moneda, Cif";
	}
	
}
