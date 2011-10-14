package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class ComboValidator extends LedElementValidator {

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return LedEntidadUtils.esLista(atributo) || LedEntidadUtils.esColeccion(atributo)
			|| LedEntidadUtils.ManyToX(atributo) || LedEntidadUtils.esSimple(atributo);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo simple, lista, coleccion o referencia (ManyToOne, ManyToMany)";
	}
	
}
