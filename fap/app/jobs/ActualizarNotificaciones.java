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
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import enumerado.fap.gen.EstadoNotificacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;

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
			    	if ((FapProperties.get("fap.notificacion.activa") != null) && (FapProperties.getBoolean("fap.notificacion.activa")) && (FapProperties.get("fap.notificacion.procedimiento") != null) && (!(FapProperties.get("fap.notificacion.procedimiento").trim().isEmpty()))){
			    		play.Logger.info("Recargando Notificaciones desde el WebService");
			    		NotificacionUtils.recargarNotificacionesFromWS(FapProperties.get("fap.notificacion.procedimiento"));
			    		
			    		// Código Añadido (05/07/2013)	CREACION automatica de las modificaciones	    		
//				    	if (FapProperties.getBoolean("fap.notificacion.activarModificacion")) {
//			    			List<SolicitudGenerica> solicitudes = SolicitudGenerica.findAll();
//			    			for (SolicitudGenerica solicitud: solicitudes) {
//			    				Notificacion notificacion = solicitud.verificacion.requerimiento.notificacion;
//			    				// Si existe una notificacion				|
//			    				// No hemos pasado de la fecha límite		|-> Activar modificacion
//			    				// La notificación esta leida				|
//			    				// No tiene un registroModificación creado	|
//			    				if ((notificacion != null) && (notificacion.activa) && 
//			    					(notificacion.fechaLimite.isAfterNow() || 
//			    					(EstadoNotificacionEnum.leida.value().equals(notificacion.estado)))) {
//			    					if (!EstadosSolicitudEnum.modificacion.name().equals(solicitud.estado)) {
//			    						RegistroModificacion registroModificacion = new RegistroModificacion();
//			    						registroModificacion.fechaCreacion = new DateTime();
//			    						registroModificacion.registro =  new Registro();
//			    						registroModificacion.save();
//			    						solicitud.activoModificacion=true;
//			    						solicitud.estadoAntesModificacion = solicitud.estado;
//			    						solicitud.registroModificacion.add(registroModificacion);
//			    						solicitud.estado = EstadosSolicitudEnum.modificacion.name();
//			    						solicitud.save();
//			    					}
			    		
	    			}
	    		}
			}
		}
	}
    
}