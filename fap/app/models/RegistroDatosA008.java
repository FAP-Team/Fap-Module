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
public class RegistroDatosA008 extends FapModel {
	// Código de los atributos

	public String tipoRegistro;

	public String regimen;

	public String cccPpal;

	public String numMedioTrabajadores;

	public String nombre;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaSolicitud"), @Column(name = "fechaSolicitudTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaSolicitud;

	public String estado;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
