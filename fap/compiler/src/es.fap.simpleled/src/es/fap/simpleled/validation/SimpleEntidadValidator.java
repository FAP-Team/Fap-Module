package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class SimpleEntidadValidator extends LedElementValidator{

	private String nameEntidad;
	
	private boolean aceptaString;
	
	public SimpleEntidadValidator(EObject element, String nameEntidad, boolean aceptaString){
		super(element);
		this.nameEntidad = nameEntidad;
		this.aceptaString = aceptaString;
	}
	
	public boolean aceptaString(){
		return aceptaString;
	}
	
	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return LedEntidadUtils.extend(entidad, nameEntidad);
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		if (LedEntidadUtils.xToOne(atributo)){
			Entity entidad = LedEntidadUtils.getEntidad(atributo);
			if (entidad != null && LedEntidadUtils.extend(entidad, nameEntidad))
				return true;
		}
		return false;
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser la entidad \"" + nameEntidad + "\" o una referencia individual a ella";
	}
	
}
