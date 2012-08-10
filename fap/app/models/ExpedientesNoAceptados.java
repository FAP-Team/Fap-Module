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

public class ExpedientesNoAceptados extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Solicitante solicitante;

	public String idAed;

	public String estado;

	@Moneda
	@Column(precision = 30, scale = 4)
	public BigDecimal cantidad;

	@Transient
	public String cantidad_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getCantidad_formatFapTabla() {
		return FapFormat.format(cantidad);
	}

	public ExpedientesNoAceptados() {
		init();
	}

	public void init() {

		if (solicitante == null)
			solicitante = new Solicitante();
		else
			solicitante.init();

		if (cantidad == null)
			cantidad = new BigDecimal(0);

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
