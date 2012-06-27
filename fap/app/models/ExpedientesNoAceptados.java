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

public class ExpedientesNoAceptados extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Solicitante solicitante;

	public String idAed;

	public String estado;

	@Moneda
	public Double Cantidad;

	public ExpedientesNoAceptados() {
		init();
	}

	public void init() {

		if (solicitante == null)
			solicitante = new Solicitante();
		else
			solicitante.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
