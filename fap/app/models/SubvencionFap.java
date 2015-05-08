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
public class SubvencionFap extends FapModel {
	// Código de los atributos

	@ValueFromTable("tiposSubvencion")
	@FapEnum("enumerado.fap.gen.TiposSubvencionEnum")
	public String tipo;

	@ValueFromTable("estadosSubvenciones")
	public String situacion;

	public String entidad;

	@Column(length = 2000)
	public String objeto;

	public String fondo;

	public String reglamento;

	public String programa;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaSolicitud"), @Column(name = "fechaSolicitudTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaSolicitud;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaAprobacion"), @Column(name = "fechaAprobacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAprobacion;

	public Double importe;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
