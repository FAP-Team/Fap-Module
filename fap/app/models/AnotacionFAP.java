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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import format.FapFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class AnotacionFAP extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fecha"), @Column(name = "fechaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaAlerta"), @Column(name = "fechaAlertaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAlerta;

	public Boolean checkResuelta;

	@Transient
	public Boolean checkAlerta;

	public Boolean alertaNotificada;

	public String tituloanotacion;

	@Column(columnDefinition = "LONGTEXT")
	public String descripcion;

	@Column(columnDefinition = "LONGTEXT")
	public String solucion;

	@ManyToOne(fetch = FetchType.LAZY)
	public Agente personaAsunto;

	@ManyToOne(fetch = FetchType.LAZY)
	public Agente personaSolucion;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
