package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;

public class EnlaceValidator extends LedElementValidator {

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return "LongText".equals(atributo.getType().getSimple()) || "String".equals(atributo.getType().getSimple());
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo String o LongText";
	}
	
}
