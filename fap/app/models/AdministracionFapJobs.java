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

	public AdministracionFapJobs() {
		init();
	}

	public void init() {
		super.init();
		actualizarNotificaciones = true;
		valorPropioActualizarNotificaciones = false;
		comprimirLogs = true;
		valorPropioComprimirLogs = false;
		eliminarTemporales = true;
		valorPropioEliminarTemporales = false;
		notificarAlertasAnotaciones = true;
		valorPropioNotificarAlertasAnotaciones = false;

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
