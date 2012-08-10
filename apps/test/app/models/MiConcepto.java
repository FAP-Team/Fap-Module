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

@Entity
public class MiConcepto extends FapModel {
	// Código de los atributos

	public Integer unInteger;

	public Double unDouble;

	@Moneda
	@Column(precision = 30, scale = 4)
	public BigDecimal unaMoneda;

	@Transient
	public String unaMoneda_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getUnaMoneda_formatFapTabla() {
		return FapFormat.format(unaMoneda);
	}

	public MiConcepto() {
		init();
	}

	public void init() {

		if (unaMoneda == null)
			unaMoneda = new BigDecimal(0);

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
