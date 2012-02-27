package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class EntidadValidator extends LedElementValidator{

	private String nameEntidad;
	
	public EntidadValidator(EObject element, String nameEntidad){
		super(element);
		this.nameEntidad = nameEntidad;
	}
	
	@Override
	public boolean aceptaEntidad(Entity entidad) {
		while (entidad != null){
			if (entidad.getName().equals(nameEntidad))
				return true;
			entidad = entidad.getExtends();
		}
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		Entity entidad = LedEntidadUtils.getEntidad(atributo);
		if (entidad != null && aceptaEntidad(entidad)){
			return true;
		}
		return false;
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser la entidad \"" + nameEntidad + "\" o una referencia individual a ella";
	}
	
}
