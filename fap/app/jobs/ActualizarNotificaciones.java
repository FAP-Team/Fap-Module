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

import models.Notificacion;

import org.joda.time.DateTime;

import play.jobs.*;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.NotificacionService;
import utils.NotificacionUtils;


/**
 * Job que actualiza la base de datos local con las notificaciones del servicio web de la ACIISI
 *
 */
@Every("5min")
public class ActualizarNotificaciones extends Job {
	
    public void doJob() {
    	if ((FapProperties.get("fap.aed.procedimientos.procedimiento.uri") != null) && (!(FapProperties.get("fap.aed.procedimientos.procedimiento.uri").trim().isEmpty())))
    		NotificacionUtils.recargarNotificacionesFromWS(FapProperties.get("fap.aed.procedimientos.procedimiento.uri"));
    }

    
}