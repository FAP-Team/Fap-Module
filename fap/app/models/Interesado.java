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
public class Interesado extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Persona persona;

	public String movil;

	public String uriTerceros;

	@Email
	public String email;

	public Boolean notificar;

	public Interesado() {
		init();
	}

	public void init() {

		if (persona == null)
			persona = new Persona();
		else
			persona.init();
		notificar = true;

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
