package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class FormValidator extends LedElementValidator{

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return LedEntidadUtils.xToMany(atributo);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser una referencia múltiple";
	}
	
}
