package controllers.popups;

import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import models.RegistroModificacion;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.PopupEditarModificacionSolicitudControllerGen;

public class PopupEditarModificacionSolicitudController extends PopupEditarModificacionSolicitudControllerGen {

	@Util
	public static void popupEditarModificacionSolicitudValidateCopy(String accion, RegistroModificacion dbRegistroModificacion, RegistroModificacion registroModificacion) {
		CustomValidation.clearValidadas();

		if (secure.checkGrafico("editarModificacionSolicitud", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("registroModificacion", registroModificacion);
			CustomValidation.required("registroModificacion.fechaLimite", registroModificacion.fechaLimite);
			
			if (!Messages.hasErrors()){
				DateTime fechaHora = registroModificacion.fechaLimite;
				fechaHora = fechaHora.withMinuteOfHour(59);
				fechaHora = fechaHora.withSecondOfMinute(59);
				fechaHora = fechaHora.withHourOfDay(23);
				
				if (fechaHora.isAfter(registroModificacion.fechaCreacion)){
					dbRegistroModificacion.fechaLimite = fechaHora;
				}
				else{ //Error de asignacion de fechas
					Messages.error("La fecha límite no puede ser anterior a la fecha de creación");
					log.error("La fecha límite no puede ser anterior a la fecha de creación");
				}
			}

		}

	}
		
}
