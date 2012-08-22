package controllers;

import play.mvc.Util;
import properties.FapProperties;
import validation.CustomValidation;
import messages.Messages;
import models.AdministracionFapJobs;
import controllers.gen.JobsControllerGen;

public class JobsController extends JobsControllerGen {

	@Util
	public static void JobsValidateCopy(String accion, AdministracionFapJobs dbAdministracionFapJobs, AdministracionFapJobs administracionFapJobs) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("administracionFapJobs", administracionFapJobs);
		
		if (properties.FapProperties.getBoolean("fap.notificacion.activa")) {
			dbAdministracionFapJobs.actualizarNotificaciones = administracionFapJobs.actualizarNotificaciones;
			
			if ((administracionFapJobs.actualizarNotificaciones != null) && (administracionFapJobs.actualizarNotificaciones == true)) {
				dbAdministracionFapJobs.valorPropioActualizarNotificaciones = administracionFapJobs.valorPropioActualizarNotificaciones;
				if ((administracionFapJobs.valorPropioActualizarNotificaciones != null) && (administracionFapJobs.valorPropioActualizarNotificaciones == true)) {
					dbAdministracionFapJobs.valorActualizarNotificaciones = administracionFapJobs.valorActualizarNotificaciones;
				} else {
					dbAdministracionFapJobs.valorActualizarNotificaciones = FapProperties.getInt("fap.notificacion.refrescoBaseDeDatosFromWS");
				}
			}
		}
		
		dbAdministracionFapJobs.comprimirLogs = administracionFapJobs.comprimirLogs;
		if ((administracionFapJobs.comprimirLogs != null) && (administracionFapJobs.comprimirLogs == true)) {
			dbAdministracionFapJobs.valorPropioComprimirLogs = administracionFapJobs.valorPropioComprimirLogs;
			if ((administracionFapJobs.valorPropioComprimirLogs != null) && (administracionFapJobs.valorPropioComprimirLogs == true)) {
				dbAdministracionFapJobs.valorComprimirLogs = administracionFapJobs.valorComprimirLogs;
			} else {
				dbAdministracionFapJobs.valorComprimirLogs = FapProperties.getInt("fap.log.compress.every");
			}
		}
		
		if (properties.FapProperties.getBoolean("fap.delete.temporals")) {
			dbAdministracionFapJobs.eliminarTemporales = administracionFapJobs.eliminarTemporales;

			if ((administracionFapJobs.eliminarTemporales != null) && (administracionFapJobs.eliminarTemporales == true)) {
				dbAdministracionFapJobs.valorPropioEliminarTemporales = administracionFapJobs.valorPropioEliminarTemporales ;
				if ((administracionFapJobs.valorPropioEliminarTemporales  != null) && (administracionFapJobs.valorPropioEliminarTemporales  == true)) {
					dbAdministracionFapJobs.valorEliminarTemporales  = administracionFapJobs.valorEliminarTemporales ;
				} else {
					dbAdministracionFapJobs.valorEliminarTemporales  = FapProperties.getInt("fap.delete.temporals.every");
				}
			}
		}
		
		dbAdministracionFapJobs.notificarAlertasAnotaciones = administracionFapJobs.notificarAlertasAnotaciones;

		if ((administracionFapJobs.notificarAlertasAnotaciones != null) && (administracionFapJobs.notificarAlertasAnotaciones == true)) {
			dbAdministracionFapJobs.valorPropioNotificarAlertasAnotaciones = administracionFapJobs.valorPropioNotificarAlertasAnotaciones;
			if ((administracionFapJobs.valorPropioNotificarAlertasAnotaciones != null) && (administracionFapJobs.valorPropioNotificarAlertasAnotaciones == true)) {
				dbAdministracionFapJobs.valorNotificarAlertasAnotaciones = administracionFapJobs.valorNotificarAlertasAnotaciones;
			} else {
				dbAdministracionFapJobs.valorNotificarAlertasAnotaciones = FapProperties.getInt("fap.seguimiento.notificarAlertar.anotaciones");
			}
		}
	}
	
}
