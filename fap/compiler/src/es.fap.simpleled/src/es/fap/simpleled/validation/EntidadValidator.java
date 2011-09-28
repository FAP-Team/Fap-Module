package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class EntidadValidator extends LedElementValidator{

	private String nameEntidad;
	
	public EntidadValidator(String nameEntidad){
		this.nameEntidad = nameEntidad;
	}
	
	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return entidad.getName().equals(nameEntidad);
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		Entity e = LedEntidadUtils.getEntidad(atributo);
		if (e != null && e.getName().equals(nameEntidad)){
			return true;
		}
		return false;
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser la entidad \"" + nameEntidad + "\" o una referencia individual a ella";
	}
	
}
