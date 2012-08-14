package controllers.popups;

import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import models.AnotacionFAP;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.fap.AgenteController;
import controllers.gen.popups.PopUpAnotacionModificarControllerGen;

public class PopUpAnotacionModificarController extends PopUpAnotacionModificarControllerGen {

	@Util
	public static void PopUpAnotacionModificarValidateCopy(String accion, AnotacionFAP dbAnotacionFAP, AnotacionFAP anotacionFAP) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("noEditable", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("anotacionFAP", anotacionFAP);
			dbAnotacionFAP.fecha = anotacionFAP.fecha;

		}
		CustomValidation.valid("anotacionFAP", anotacionFAP);
		CustomValidation.required("anotacionFAP.tituloanotacion", anotacionFAP.tituloanotacion);
		dbAnotacionFAP.tituloanotacion = anotacionFAP.tituloanotacion;
		CustomValidation.required("anotacionFAP.descripcion", anotacionFAP.descripcion);
		dbAnotacionFAP.descripcion = anotacionFAP.descripcion;
		dbAnotacionFAP.checkResuelta = anotacionFAP.checkResuelta;
		
		dbAnotacionFAP.checkAlerta = anotacionFAP.checkAlerta;
		
		if ((anotacionFAP.checkResuelta != null) && (anotacionFAP.checkResuelta == true)) {
			CustomValidation.required("anotacionFAP.solucion", anotacionFAP.solucion);
			dbAnotacionFAP.solucion = anotacionFAP.solucion;
			dbAnotacionFAP.personaSolucion = AgenteController.getAgente();
		} else {
			dbAnotacionFAP.solucion =null;
			dbAnotacionFAP.personaSolucion=null;
		}
		
		if ((anotacionFAP.fechaAlerta != null)) {
			if (anotacionFAP.fechaAlerta.getMillis() < new DateTime().getMillis()) {
				CustomValidation.error("La fecha de la alerta no puede ser anterior a la actual.", "anotacionFAP.fechaAlerta", anotacionFAP.fechaAlerta);
				return;
			} else if ((dbAnotacionFAP.fechaAlerta != null) && (! dbAnotacionFAP.fechaAlerta.equals(anotacionFAP.fechaAlerta)) && (dbAnotacionFAP.fechaAlerta.getMillis() < new DateTime().getMillis())) {
				Messages.error("No se puede modificar la fecha de alerta de la anotación porque ya ha expirado.");
			}
		}

	}

	
}
