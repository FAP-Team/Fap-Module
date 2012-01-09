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
		return /*!LedEntidadUtils.esSingleton(entidad)*/ true;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		Entity valida = LedCampoUtils.getEntidadPaginaOrPopupOrTabla(element);
		Entity actual = LedCampoUtils.getCampo(element).getEntidad();
		if (actual == null)
			actual = raiz;
		if ((valida == null || !LedEntidadUtils.equals(valida, actual)) && !LedEntidadUtils.esSingleton(actual))
			return false; 
		return LedEntidadUtils.xToMany(atributo);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser una entidad no singleton, o una referencia múltiple";
	}
	
}
