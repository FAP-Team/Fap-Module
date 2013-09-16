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
public class JsonPeticionModificacion extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	@Column(columnDefinition = "LONGTEXT")
	public String jsonPeticion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRestauracion"), @Column(name = "fechaRestauracionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRestauracion;

	public Boolean restaurado;

	public JsonPeticionModificacion() {
		init();
	}

	public void init() {

		if (restaurado == null)
			restaurado = false;

		postInit();
	}

	// === MANUAL REGION START ===

	public void postInit() {

		if (fechaCreacion == null)
			fechaCreacion = new DateTime().now();

	}
	// === MANUAL REGION END ===

}
