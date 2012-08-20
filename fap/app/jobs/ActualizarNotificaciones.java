package jobs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import models.AdministracionFapJobs;
import models.Notificacion;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import play.db.jpa.Transactional;
import play.jobs.*;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.NotificacionService;
import utils.NotificacionUtils;


/**
 * Job que actualiza la base de datos local con las notificaciones del servicio web de la ACIISI
 *
 */
@Every("1min")
public class ActualizarNotificaciones extends Job {
	
	static Integer tiempoRefresco = 1;
	
	@Transactional
    public void doJob() {
		if (AdministracionFapJobs.all() != null) {
			AdministracionFapJobs job = AdministracionFapJobs.all().first();
			if (job.actualizarNotificaciones) {
		    	if ((job.valorActualizarNotificaciones != null) && (tiempoRefresco == job.valorActualizarNotificaciones)){
		    		tiempoRefresco=1;
			    	if ((FapProperties.get("fap.notificacion.activa") != null) && (FapProperties.getBoolean("fap.notificacion.activa")) && (FapProperties.get("fap.notificacion.procedimiento") != null) && (!(FapProperties.get("fap.notificacion.procedimiento").trim().isEmpty())))
			    		NotificacionUtils.recargarNotificacionesFromWS(FapProperties.get("fap.notificacion.procedimiento"));
		    	} else {
		    		tiempoRefresco++;
		    	}
			}
		}
    }

    
}