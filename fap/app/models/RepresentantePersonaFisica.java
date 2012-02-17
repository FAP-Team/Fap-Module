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

@Auditable
@Entity
public class RepresentantePersonaFisica extends Persona {
	// Código de los atributos

	public String telefonoFijo;

	public String telefonoMovil;

	public String fax;

	@Email
	public String email;

	public void init() {
		super.init();

	}

	// === MANUAL REGION START ===
	@PostPersist
	public void print() {
		play.Logger.info("Se guardó el representante [" + this.toString() + "]");
	}

	@PrePersist
	public void printPre() {
		play.Logger.info("Se va a guardar el representante [" + this.toString() + "]");
	}
	// === MANUAL REGION END ===

}
