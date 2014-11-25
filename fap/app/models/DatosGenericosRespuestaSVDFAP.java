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
public class DatosGenericosRespuestaSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EmisorSVDFAP emisor;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitanteSVDFAP solicitante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TitularSVDFAP titular;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TransmisionSVDFAP transmision;

	public DatosGenericosRespuestaSVDFAP() {
		init();
	}

	public void init() {

		if (emisor == null)
			emisor = new EmisorSVDFAP();
		else
			emisor.init();

		if (solicitante == null)
			solicitante = new SolicitanteSVDFAP();
		else
			solicitante.init();

		if (titular == null)
			titular = new TitularSVDFAP();
		else
			titular.init();

		if (transmision == null)
			transmision = new TransmisionSVDFAP();
		else
			transmision.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
