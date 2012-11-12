package controllers.popups;

import messages.Messages;
import models.Trabajador;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.PopupTrabajadoresControllerGen;
import utils.StringUtils;

public class PopupTrabajadoresController extends PopupTrabajadoresControllerGen {

	@Util
	public static void PopupTrabajadoresValidateCopy(String accion, Trabajador dbTrabajador, Trabajador trabajador) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("trabajador", trabajador);
		CustomValidation.required("trabajador.regimen", trabajador.regimen);
		CustomValidation.required("trabajador.codigoCuenta", trabajador.codigoCuenta);
		
		if (!StringUtils.validarCuentaCotizacion(trabajador.codigoCuenta))
			Messages.error("Error, compruebe el Código de Cuenta de Cotización");

		if (!StringUtils.validarRegimen(trabajador.regimen))
			Messages.error("Error, compruebe el Régimen de la Seguridad Social");
		
		dbTrabajador.codigoCuenta = trabajador.codigoCuenta;
		dbTrabajador.regimen = trabajador.regimen;

	}	
	
	
	
}
