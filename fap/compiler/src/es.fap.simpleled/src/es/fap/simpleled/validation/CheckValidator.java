package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class CheckValidator extends LedElementValidator {

	public CheckValidator(EObject element) {
		super(element);
	}

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		if (("Boolean".equals(LedEntidadUtils.getSimpleTipo(atributo))) || ("boolean".equals(LedEntidadUtils.getSimpleTipo(atributo))))
			return true;
		else
			return false;
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo Boolean o boolean";
	}
	
}
