package jobs;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.joda.time.DateTime;


import com.jamonapi.utils.Logger;

import emails.Mails;
import es.gobcan.eadmon.aed.ws.dominio.Solicitud;

import models.AdministracionFapJobs;
import models.AnotacionFAP;
import models.TableKeyValue;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.jobs.*;
import properties.FapProperties;

@Every("1min")
public class NotificarAlertasAnotacionesFAP extends Job implements LocalVariablesSupport {

	static Integer tiempoRefresco = 1;
	
	@Transactional
	public void doJob() {
		if (AdministracionFapJobs.all() != null) {
			AdministracionFapJobs job = AdministracionFapJobs.all().first();
			if ((job.notificarAlertasAnotaciones != null) && (job.notificarAlertasAnotaciones == true)) {
				if ((job.valorNotificarAlertasAnotaciones != null) && (tiempoRefresco.equals(job.valorNotificarAlertasAnotaciones))){
					tiempoRefresco=1;
					List<AnotacionFAP> anotaciones = new ArrayList<AnotacionFAP>();
					try {
						play.Logger.info("Comprobando alertas anotaciones pendientes a las: " + new DateTime().toString());
						anotaciones = AnotacionFAP.find("select anotacion from AnotacionFAP anotacion where ((anotacion.alertaNotificada is NULL) or (anotacion.alertaNotificada = false))").fetch();
					} catch (Exception ex) {
						play.Logger.error(ex, "Error al consultar las alertas de anotaciones que no han expirado.");
						return;
					}
			
					for (AnotacionFAP anotacion : anotaciones) {
						try {
							if (anotacion.fechaAlerta.isBeforeNow()) {
								play.Logger.info("Correo send to:" +  anotacion.personaAsunto.email);
								play.Logger.info("Fecha:" +  anotacion.fecha);
								play.Logger.info("Fecha Alerta:" +  anotacion.fechaAlerta);
								play.Logger.info("Titulo:" +  anotacion.tituloanotacion);
								play.Logger.info("Descripción:" +  anotacion.descripcion);
			
								play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("anotacion", anotacion);
								Mails.enviar("anotacion", anotacion);
								anotacion.alertaNotificada = true;
								anotacion.save();
								play.Logger.debug("Nueva notificación de expiración de una alerta: email");
							}
						} catch (Exception e) {
							play.Logger.error("No se pudo enviar el correo de la anotación con identificador '"	+ anotacion.id + "'. "+e);
						}
					}
					return;
				} else {
		    		tiempoRefresco++;
		    	}
			}
		}
		
	}
}
