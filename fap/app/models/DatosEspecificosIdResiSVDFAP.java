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
public class DatosEspecificosIdResiSVDFAP extends FapModel {
	// Código de los atributos

	public String tipoSolicitante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudEspecificaSVDFAP solicitud;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DomicilioSVDFAP domicilio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TitularSVDFAP datosTitular;

	public DatosEspecificosIdResiSVDFAP() {
		init();
	}

	public void init() {

		if (solicitud == null)
			solicitud = new SolicitudEspecificaSVDFAP();
		else
			solicitud.init();

		if (domicilio == null)
			domicilio = new DomicilioSVDFAP();
		else
			domicilio.init();

		if (datosTitular == null)
			datosTitular = new TitularSVDFAP();
		else
			datosTitular.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
