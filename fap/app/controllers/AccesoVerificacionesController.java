package controllers;

import org.joda.time.DateTime;

import messages.Messages;
import models.SolicitudGenerica;
import models.Verificacion;
import controllers.gen.AccesoVerificacionesControllerGen;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class AccesoVerificacionesController extends AccesoVerificacionesControllerGen {
	
	public static void verificacionNueva(Long idSolicitud) {
		checkAuthenticity();
		// Save code
		if (permisoverificacionNueva("update") || permisoverificacionNueva("create")) {
			if (!validation.hasErrors()) {
				SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
				// Asignamos una nueva verificacion con su fecha de creacion, cuando se pulse el boton de nueva verificación
				dbSolicitud.verificacion = new Verificacion();
				dbSolicitud.verificacion.expediente = dbSolicitud.expedienteAed.idAed;
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.iniciada.name();
				dbSolicitud.verificacion.fechaCreacion = new DateTime();
				dbSolicitud.save();
				redirect("VerificacionController.index", idSolicitud);
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		verificacionNuevaRender(idSolicitud);
	}

}
