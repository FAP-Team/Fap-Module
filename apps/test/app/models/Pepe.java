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
public class Pepe extends Model {
	// Código de los atributos

	@ElementCollection
	@ValueFromTable("LstClaseCriterio")
	public Set<String> f;

	public Pepe() {
		init();
	}

	public void init() {

		if (f == null)
			f = new HashSet<String>();

	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
