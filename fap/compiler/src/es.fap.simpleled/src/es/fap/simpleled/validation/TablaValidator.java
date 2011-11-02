package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class TablaValidator extends LedElementValidator{

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return !LedEntidadUtils.esSingleton(entidad);
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return LedEntidadUtils.xToMany(atributo);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser una entidad o una referencia múltiple";
	}
	
}
