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
public class Cesiones extends FapModel {
	// Código de los atributos

	@ValueFromTable("listaCesiones")
	public String tipo;

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

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documento;

	public boolean firmada;

	public Cesiones() {
		init();
	}

	public void init() {

		if (documento == null)
			documento = new Documento();
		else
			documento.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
