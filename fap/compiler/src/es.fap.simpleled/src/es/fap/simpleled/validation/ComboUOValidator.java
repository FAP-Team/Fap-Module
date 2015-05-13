package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class ComboUOValidator  extends LedElementValidator{

	public ComboUOValidator(EObject element) {
		super(element);
	}

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
	
	@Override
	public void validateCampo(Campo campo, LedJavaValidator validator) {
		Entity entidad = LedCampoUtils.getUltimaEntidad(campo);
		
		if (!entidad.getName().equals("ReturnUnidadOrganicaFap"))
			validator.myError("La entidad de destino debe ser del tipo ReturnUnidadOrganicaFap", campo, null, 0);
	}

}
