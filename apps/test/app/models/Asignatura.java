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
public class Asignatura extends FapModel {
	// Código de los atributos

	public Long codigo;

	public String nombre;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "asignatura_temas")
	public List<Tema> temas;

	public Asignatura() {
		init();
	}

	public void init() {

		if (temas == null)
			temas = new ArrayList<Tema>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
