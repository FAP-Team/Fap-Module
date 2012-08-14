package jobs;

import java.util.List;

import javax.persistence.Query;

import org.joda.time.DateTime;


import com.jamonapi.utils.Logger;

import emails.Mails;
import es.gobcan.eadmon.aed.ws.dominio.Solicitud;

import models.AnotacionFAP;
import models.TableKeyValue;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPA;
import play.jobs.*;
import properties.FapProperties;

@Every("3min")
public class NotificarAlertasAnotaciones extends Job {

	@Override
	public void doJob() {

		play.Logger.info("Comprobando alertas anotaciones pendientes a las: " + new DateTime().toString());
		Query query = JPA.em().createQuery(
						"SELECT A FROM AnotacionFAP A WHERE (A.alertaNotificada!=true) AND (A.fechaAlerta <= :fechaVencimiento)");
		query.setParameter("fechaVencimiento", new DateTime());

		List<AnotacionFAP> anotaciones = null;
		try {
			anotaciones = query.getResultList();
		} catch (Exception ex) {
			play.Logger.error(ex, "Error al consultar las alertas de anotaciones que no han expirado.");
			return;
		}

		for (AnotacionFAP anotacion : anotaciones) {
			try {
				play.Logger.info("Correo send to:" +  anotacion.personaAsunto.email);
				play.Logger.info("Fecha:" +  anotacion.fecha);
				play.Logger.info("Fecha Alerta:" +  anotacion.fechaAlerta);
				play.Logger.info("Titulo:" +  anotacion.tituloanotacion);
				play.Logger.info("Descripción:" +  anotacion.descripcion);

				Mails.enviar("anotacion", anotacion);
				anotacion.alertaNotificada = true;
				anotacion.save();
				play.Logger.debug("Nueva notificación de expiración de una alerta: email");
			} catch (Exception ex) {
				play.Logger.error("No se pudo enviar el correo de la anotación con identificador '"	+ anotacion.id + "'.");
			}
		}
		return;
	}
}
