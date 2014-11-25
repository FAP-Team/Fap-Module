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
public class RespuestaSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AtributosRespuestaSVDFAP atributos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TransmisionesRespuestaSVDFAP transmisiones;

	public RespuestaSVDFAP() {
		init();
	}

	public void init() {

		if (atributos == null)
			atributos = new AtributosRespuestaSVDFAP();
		else
			atributos.init();

		if (transmisiones == null)
			transmisiones = new TransmisionesRespuestaSVDFAP();
		else
			transmisiones.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
