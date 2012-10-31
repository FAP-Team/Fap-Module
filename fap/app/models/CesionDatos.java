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
public class CesionDatos extends FapModel {
	// Código de los atributos

	public Boolean aeat;

	public Boolean inssR001;

	public Boolean atc;

	public Boolean inssA008;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "cesiondatos_trabajadores")
	public List<Trabajador> trabajadores;

	public Boolean idi;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaPeticion"), @Column(name = "fechaPeticionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaPeticion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaValidez"), @Column(name = "fechaValidezTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaValidez;

	@ValueFromTable("listaEstados")
	public String estado;

	@ValueFromTable("listaOrigen")
	public String origen;

	public CesionDatos() {
		init();
	}

	public void init() {

		if (trabajadores == null)
			trabajadores = new ArrayList<Trabajador>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
