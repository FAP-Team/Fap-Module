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
public class DatosGenericosPeticion extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitantePeticion solicitante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TitularPeticion titular;

	public DatosGenericosPeticion() {
		init();
	}

	public void init() {

		if (solicitante == null)
			solicitante = new SolicitantePeticion();
		else
			solicitante.init();

		if (titular == null)
			titular = new TitularPeticion();
		else
			titular.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
