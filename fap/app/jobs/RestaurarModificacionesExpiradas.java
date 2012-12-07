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

import messages.Messages;
import models.AdministracionFapJobs;
import models.Notificacion;
import models.RegistroModificacion;
import models.SolicitudGenerica;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import emails.Mails;

import play.db.jpa.Transactional;
import play.jobs.*;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.NotificacionService;
import utils.ModelUtils;
import utils.NotificacionUtils;


/**
 * Job que se encarga de restaurar las modificaciones en caso de que haya expirado la fecha limite y no esté registrada 
 *
 */

@On("0 0 0 * * ?")
public class RestaurarModificacionesExpiradas extends Job {
	
	@Transactional
    public void doJob() {
		List<SolicitudGenerica> solicitudes = SolicitudGenerica.findAll();
		for (SolicitudGenerica solicitud: solicitudes){
			if ((!solicitud.registroModificacion.isEmpty()) && (!solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro.fasesRegistro.registro) && (solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).fechaLimite.isAfterNow())){
				play.Logger.info("La solicitud "+solicitud.id+" va a ser restaurada a un estado anterior porque no ha sido presentada y la fecha de modificación ha expirado "+solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).fechaLimite.toString());
				ModelUtils.restaurarSolicitud(solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id, solicitud.id, false);
				if (!Messages.hasErrors()){
					try {
						play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
						Mails.enviar("solicitudRestauradaAuto", solicitud);
						play.Logger.info("Correo Solicitud "+solicitud.id+" Modificada por Expiración de Fecha enviado");
					} catch (Exception e){
						play.Logger.error("Envío del Mail de solicitud "+solicitud.id+" restaurada por expiración de fecha límite fallido: "+e.getMessage());
					}
				} else {
					try {
						play.Logger.error("La solicitud "+solicitud.id+" que debió ser restaurada a un estado anterior porque no ha sido presentada y la fecha de modificación ha expirado "+solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).fechaLimite.toString()+", NO ha sido restaurada con éxito");
						play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
						Mails.enviar("solicitudRestauradaAutoFallo", solicitud);
					} catch (Exception e){
						play.Logger.error("Envío del Mail de solicitud "+solicitud.id+" restaurada sin éxito por problemas de expiración de fecha límite fallido: "+e.getMessage());
					}
				}
			}
		}
    }

    
}