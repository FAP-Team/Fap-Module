package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class WSReturnValidator extends LedElementValidator{

	public WSReturnValidator(EObject element) {
		super(element);
	}

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}
	
	@Override
	public boolean aceptaString() {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return LedEntidadUtils.getEntidad(atributo) == null;
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo simple o tipo lista";
	}
	
}
