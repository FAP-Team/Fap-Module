package es.fap.simpleled.validation;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class ComboValidator extends LedElementValidator {

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return false;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return LedEntidadUtils.esLista(atributo) || LedEntidadUtils.esColeccion(atributo);
	}

	@Override
	public String mensajeError() {
		return "El campo tiene que ser de tipo lista o coleccion";
	}
	
	@Override
	public void validateCampo(Campo campo, LedJavaValidator validator) {
		Attribute attr = LedCampoUtils.getUltimoAtributo(campo);
		if (attr != null && attr.getType().getSimple() != null){
//			validator.myWarning("Con un campo de tipo simple, tendrá que rellenar los valores de la lista manualmente", campo, null, 0);
		}
		else{
			super.validateCampo(campo, validator);
		}
	}
	
}
