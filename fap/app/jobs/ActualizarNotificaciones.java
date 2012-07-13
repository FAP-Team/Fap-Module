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
@Every("1min")
public class ActualizarNotificaciones extends Job {
	
	Integer tiempoRefresco=1;
	
    public void doJob() {
    	
    	Integer frecuencia = FapProperties.getInt("fap.notificacion.refrescoBaseDeDatosFromWS");
    	if ((frecuencia != null) && (tiempoRefresco == frecuencia)){
    		tiempoRefresco=1;
	    	if ((FapProperties.get("fap.notificacion.activa") != null) && (FapProperties.getBoolean("fap.notificacion.activa")) && (FapProperties.get("fap.notificacion.procedimiento") != null) && (!(FapProperties.get("fap.notificacion.procedimiento").trim().isEmpty())))
	    		NotificacionUtils.recargarNotificacionesFromWS(FapProperties.get("fap.notificacion.procedimiento"));
    	} else {
    		tiempoRefresco++;
    	}
    }

    
}