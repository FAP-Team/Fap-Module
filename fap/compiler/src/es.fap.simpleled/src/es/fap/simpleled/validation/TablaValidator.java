package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class TablaValidator extends LedElementValidator{

	public TablaValidator(EObject element) {
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
		Entity actual = LedCampoUtils.getCampo(element).getEntidad();
		if (actual == null)
			actual = raiz;
		for (Entity valida: LedCampoUtils.getEntidadesValidas(element.eContainer()).values()){
			if (LedEntidadUtils.equals(valida, actual))
				return LedEntidadUtils.xToMany(atributo);
		}
		return false;
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser una entidad o una referencia múltiple";
	}
	
}
