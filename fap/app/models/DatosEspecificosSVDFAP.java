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
public class DatosEspecificosSVDFAP extends FapModel {
	// Código de los atributos

	public String tipoSolicitante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudEspecificaSVDFAP solicitud;

	public DatosEspecificosSVDFAP() {
		init();
	}

	public void init() {

		if (solicitud == null)
			solicitud = new SolicitudEspecificaSVDFAP();
		else
			solicitud.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
