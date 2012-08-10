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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import format.FapFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

public class ExpedientesAlegados extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Solicitante solicitante;

	public String idAed;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRegistro"), @Column(name = "fechaRegistroTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;

	@ValueFromTable("estadoAlegacion")
	public String estado;

	public ExpedientesAlegados() {
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
