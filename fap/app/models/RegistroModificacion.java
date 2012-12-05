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
public class RegistroModificacion extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaLimite"), @Column(name = "fechaLimiteTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaLimite;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "registromodificacion_jsonpeticionesmodificacion")
	public List<JsonPeticionModificacion> jsonPeticionesModificacion;

	@Transient
	public String estado;

	public RegistroModificacion() {
		init();
	}

	public void init() {

		if (jsonPeticionesModificacion == null)
			jsonPeticionesModificacion = new ArrayList<JsonPeticionModificacion>();

		postInit();
	}

	// === MANUAL REGION START ===

	public String getEstado() {
		if (this.registro.fasesRegistro.registro)
			return "Finalizada";
		else
			return "En Curso";
	}

	// === MANUAL REGION END ===

}
