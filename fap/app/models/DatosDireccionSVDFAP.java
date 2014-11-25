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
public class DatosDireccionSVDFAP extends FapModel {
	// Código de los atributos

	public String localidad;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ProvinciaSVDFAP provincia;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ViaSVDFAP datosVia;

	public DatosDireccionSVDFAP() {
		init();
	}

	public void init() {

		if (provincia == null)
			provincia = new ProvinciaSVDFAP();
		else
			provincia.init();

		if (datosVia == null)
			datosVia = new ViaSVDFAP();
		else
			datosVia.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
