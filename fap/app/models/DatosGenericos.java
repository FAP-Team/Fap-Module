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
public class DatosGenericos extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EmisorRespuesta emisor;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitanteRespuesta solicitante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TitularRespuesta titular;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TransmisionRespuesta transmision;

	public DatosGenericos() {
		init();
	}

	public void init() {

		if (emisor == null)
			emisor = new EmisorRespuesta();
		else
			emisor.init();

		if (solicitante == null)
			solicitante = new SolicitanteRespuesta();
		else
			solicitante.init();

		if (titular == null)
			titular = new TitularRespuesta();
		else
			titular.init();

		if (transmision == null)
			transmision = new TransmisionRespuesta();
		else
			transmision.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
