package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GrupoValidator extends LedElementValidator{

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return LedEntidadUtils.esSimple(atributo) || LedEntidadUtils.esLista(atributo);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo simple o tipo lista";
	}
	
}
