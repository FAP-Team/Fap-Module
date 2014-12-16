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
public class SolicitudEspecificaSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ResidenciaSVDFAP residencia;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public NacimientoSVDFAP solicitudNacimiento;

	@ValueFromTable("Espanol")
	public String espanol;

	public SolicitudEspecificaSVDFAP() {
		init();
	}

	public void init() {

		if (residencia == null)
			residencia = new ResidenciaSVDFAP();
		else
			residencia.init();

		if (solicitudNacimiento == null)
			solicitudNacimiento = new NacimientoSVDFAP();
		else
			solicitudNacimiento.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
