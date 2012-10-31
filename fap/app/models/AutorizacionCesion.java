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
public class AutorizacionCesion extends FapModel {
	// Código de los atributos

	public Boolean aeat;

	public Boolean inssR001;

	public Boolean atc;

	public Boolean inssA008;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "autorizacioncesion_trabajadores")
	public List<Trabajador> trabajadores;

	public Boolean idi;

	public AutorizacionCesion() {
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
