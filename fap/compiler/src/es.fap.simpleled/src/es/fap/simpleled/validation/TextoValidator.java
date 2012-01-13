package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class TextoValidator extends LedElementValidator{

	public TextoValidator(EObject element) {
		super(element);
	}

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		String simple = LedEntidadUtils.getSimpleTipo(atributo);
		return (
			"String".equals(simple) ||
			"Long".equals(simple) ||
			"Integer".equals(simple) ||
			"Double".equals(simple) ||
			"LongText".equals(simple) ||
			"Telefono".equals(simple) ||
			"Email".equals(simple) ||
			"Moneda".equals(simple) ||
			"Cif".equals(simple)
		);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de alguno de los siguientes tipos: String, LongText, Integer, Double, LongText, Telefono, Email, Moneda, Cif";
	}
	
}
