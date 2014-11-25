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
public class DireccionRespuestaSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ViaSVDFAP via;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public NumeroSVDFAP numero;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public NumeroSuperiorSVDFAP numeroSuperior;

	public String bloque;

	public String codigoPostal;

	public String escalera;

	public String hmt;

	public String kmt;

	public String planta;

	public String portal;

	public String puerta;

	public DireccionRespuestaSVDFAP() {
		init();
	}

	public void init() {

		if (via == null)
			via = new ViaSVDFAP();
		else
			via.init();

		if (numero == null)
			numero = new NumeroSVDFAP();
		else
			numero.init();

		if (numeroSuperior == null)
			numeroSuperior = new NumeroSuperiorSVDFAP();
		else
			numeroSuperior.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
