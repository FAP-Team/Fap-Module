package controllers.popups;

import messages.Messages;
import models.AnotacionFAP;

import org.joda.time.DateTime;

import play.mvc.Util;

import validation.CustomValidation;
import controllers.fap.AgenteController;
import controllers.gen.popups.PopUpAnotacionCrearControllerGen;

public class PopUpAnotacionCrearController extends PopUpAnotacionCrearControllerGen {

	@Util
	public static void PopUpAnotacionCrearValidateCopy(String accion, AnotacionFAP dbAnotacionFAP, AnotacionFAP anotacionFAP) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("anotacionFAP", anotacionFAP);
		CustomValidation.required("anotacionFAP.tituloanotacion", anotacionFAP.tituloanotacion);
		dbAnotacionFAP.tituloanotacion = anotacionFAP.tituloanotacion;
		dbAnotacionFAP.checkResuelta = anotacionFAP.checkResuelta;
		CustomValidation.required("anotacionFAP.descripcion", anotacionFAP.descripcion);
		dbAnotacionFAP.descripcion = anotacionFAP.descripcion;
		dbAnotacionFAP.checkAlerta = anotacionFAP.checkAlerta;
		
		dbAnotacionFAP.personaAsunto = AgenteController.getAgente();
		// Fecha de creación de la anotación
		dbAnotacionFAP.fecha = new DateTime();
		
		if ((anotacionFAP.checkAlerta != null) && (anotacionFAP.checkAlerta == true)) {
			CustomValidation.required("anotacionFAP.fechaAlerta", anotacionFAP.fechaAlerta);
			if ((anotacionFAP.fechaAlerta != null) && (anotacionFAP.fechaAlerta.getMillis() < new DateTime().getMillis())) {
				CustomValidation.error("Fecha de Alerta: La fecha de la alerta no puede ser anterior a la actual.", "anotacionFAP.fechaAlerta", anotacionFAP.fechaAlerta);
				//Messages.error("Fecha de Alerta: La fecha de la alerta no puede ser anterior a la actual.");
				return;
			}
			dbAnotacionFAP.fechaAlerta = anotacionFAP.fechaAlerta;
		}
	}
	
}
