package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===
import properties.FapProperties;

// === IMPORT REGION END ===

@Entity
public class AdministracionFapJobs extends Singleton {
	// Código de los atributos

	public Boolean actualizarNotificaciones;

	public Boolean valorPropioActualizarNotificaciones;

	public Integer valorActualizarNotificaciones;

	public Boolean comprimirLogs;

	public Boolean valorPropioComprimirLogs;

	public Integer valorComprimirLogs;

	public Boolean eliminarTemporales;

	public Boolean valorPropioEliminarTemporales;

	public Integer valorEliminarTemporales;

	public Boolean notificarAlertasAnotaciones;

	public Boolean valorPropioNotificarAlertasAnotaciones;

	public Integer valorNotificarAlertasAnotaciones;

	public Boolean actualizarServiciosWeb;

	public Boolean valorPropioActualizarServiciosWeb;

	public Integer valorActualizarServiciosWeb;

	public AdministracionFapJobs() {
		init();
	}

	public void init() {
		super.init();

		if (actualizarNotificaciones == null)
			actualizarNotificaciones = true;

		if (valorPropioActualizarNotificaciones == null)
			valorPropioActualizarNotificaciones = false;

		if (comprimirLogs == null)
			comprimirLogs = true;

		if (valorPropioComprimirLogs == null)
			valorPropioComprimirLogs = false;

		if (eliminarTemporales == null)
			eliminarTemporales = true;

		if (valorPropioEliminarTemporales == null)
			valorPropioEliminarTemporales = false;

		if (notificarAlertasAnotaciones == null)
			notificarAlertasAnotaciones = true;

		if (valorPropioNotificarAlertasAnotaciones == null)
			valorPropioNotificarAlertasAnotaciones = false;

		if (actualizarServiciosWeb == null)
			actualizarServiciosWeb = true;

		if (valorPropioActualizarServiciosWeb == null)
			valorPropioActualizarServiciosWeb = false;

		postInit();
	}

	// === MANUAL REGION START ===

	public void postInit() {
		valorActualizarNotificaciones = FapProperties.getInt("fap.notificacion.refrescoBaseDeDatosFromWS");
		valorComprimirLogs = FapProperties.getInt("fap.log.compress.every");
		valorEliminarTemporales = FapProperties.getInt("fap.delete.temporals.every");
		valorNotificarAlertasAnotaciones = FapProperties.getInt("fap.seguimiento.notificarAlertar.anotaciones");
	}

	// === MANUAL REGION END ===

}
