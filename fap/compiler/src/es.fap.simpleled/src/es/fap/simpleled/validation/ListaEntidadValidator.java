package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class ListaEntidadValidator extends LedElementValidator{

	private String nameEntidad;
	
	public ListaEntidadValidator(EObject element, String nameEntidad){
		super(element);
		this.nameEntidad = nameEntidad;
	}
	
	@Override
	public boolean aceptaString() {
		return false;
	}
	
	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return LedEntidadUtils.extend(entidad, nameEntidad);
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		if (LedEntidadUtils.xToMany(atributo)){
			Entity entidad = LedEntidadUtils.getEntidad(atributo);
			if (entidad != null && LedEntidadUtils.extend(entidad, nameEntidad))
				return true;
		}
		return false;
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser la entidad \"" + nameEntidad + "\" o una referencia múltiple a ella";
	}
	
}
