package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class FechaValidator extends LedElementValidator{

	public FechaValidator(EObject element) {
		super(element);
	}

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return "DateTime".equals(LedEntidadUtils.getSimpleTipo(atributo));
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo DateTime";
	}
	
}
