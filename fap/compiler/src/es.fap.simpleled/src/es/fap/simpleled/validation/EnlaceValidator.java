package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class EnlaceValidator extends LedElementValidator {

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		String simple = LedEntidadUtils.getSimpleTipo(atributo);
		return "LongText".equals(simple) || "String".equals(simple);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo String o LongText";
	}
	
}
