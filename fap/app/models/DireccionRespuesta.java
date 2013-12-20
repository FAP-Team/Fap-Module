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
public class DireccionRespuesta extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Via via;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Numero numero;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public NumeroSuperior numeroSuperior;

	public String bloque;

	public String codigoPostal;

	public String escalera;

	public String hmt;

	public String kmt;

	public String planta;

	public String portal;

	public String puerta;

	public DireccionRespuesta() {
		init();
	}

	public void init() {

		if (via == null)
			via = new Via();
		else
			via.init();

		if (numero == null)
			numero = new Numero();
		else
			numero.init();

		if (numeroSuperior == null)
			numeroSuperior = new NumeroSuperior();
		else
			numeroSuperior.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
