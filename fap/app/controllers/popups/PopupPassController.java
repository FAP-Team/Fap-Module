package controllers.popups;

import java.util.Map;

import models.Agente;
import play.data.validation.Validation;
import play.libs.Crypto;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.PopupPassControllerGen;

public class PopupPassController extends PopupPassControllerGen {

	@Util
	public static void PopupPassValidateCopy(String accion, Agente dbAgente, Agente agente) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("noEditable", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("agente", agente);
			dbAgente.username = agente.username;
		}
		
		CustomValidation.valid("agente", agente);
		CustomValidation.required("agente.verificacion", agente.verificacion);
		CustomValidation.required("agente.newpassword", agente.newpassword);
		CustomValidation.compare(agente.newpassword, params.get("agente_newpasswordcopy"));
		
		if (agente.verificacion != null) {		// agente.verificación = contraseña actual 
			if ( dbAgente.password.equals(Crypto.passwordHash(agente.verificacion)) ) {
				dbAgente.password = Crypto.passwordHash(agente.newpassword);
				log.info("El usuario <" + dbAgente.username + "> ha modificado su contraseña");
			} 
			else {
				Validation.addError("password", "Contraseña actual incorrecta");
			}
		}
//		else {			
//			dbAgente.password = Crypto.passwordHash(agente.newpassword);
//			log.info("El usuario <" + dbAgente.username + "> ha modificado su contraseña");
//		}
	}

}
