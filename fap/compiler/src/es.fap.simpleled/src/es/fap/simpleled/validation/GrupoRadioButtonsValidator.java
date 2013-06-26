package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.Check;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.GrupoRadioButtons;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.RadioButton;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GrupoRadioButtonsValidator extends LedElementValidator {
	
	public GrupoRadioButtonsValidator(EObject element) {
		super(element);
	}

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return "String".equals(LedEntidadUtils.getSimpleTipo(atributo))
				|| LedEntidadUtils.esLista(atributo)
				|| LedEntidadUtils.esColeccion(atributo)
				|| LedEntidadUtils.ManyToX(atributo);
	}
	
	@Override
	public String mensajeError() {
		return "El campo debe ser de tipo String, Lista o Referencia (ManyToOne o ManyToMany)";
	}

}
