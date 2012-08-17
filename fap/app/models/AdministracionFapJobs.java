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

// === IMPORT REGION END ===

@Entity
public class AdministracionFapJobs extends Singleton {
	// Código de los atributos

	public Boolean actualizarNotificaciones;

	public Boolean comprimirLogs;

	public Boolean eliminarTemporales;

	public AdministracionFapJobs() {
		init();
	}

	public void init() {
		super.init();
		actualizarNotificaciones = true;
		comprimirLogs = true;
		eliminarTemporales = true;

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
