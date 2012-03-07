package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class EntidadAutomaticaValidator extends LedElementValidator{

	public EntidadAutomaticaValidator(EObject element) {
		super(element);
	}

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return true;
	}

	@Override
	public boolean aceptaString() {
		return false;
	}
	
	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return LedEntidadUtils.xToOne(atributo);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser una entidad o una referencia individual";
	}
	
}
