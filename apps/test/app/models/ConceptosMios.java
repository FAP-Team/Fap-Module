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
public class ConceptosMios extends FapModel {
	// Código de los atributos

	@Moneda
	public Double moneda1;

	@Moneda
	public Double moneda2;

	@Moneda
	public Double moneda3;

	@Moneda
	public Double moneda4;

	@Transient
	public String moneda1_formatFapTabla;

	@Transient
	public String moneda2_formatFapTabla;

	@Transient
	public String moneda3_formatFapTabla;

	@Transient
	public String moneda4_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getMoneda1_formatFapTabla() {
		return format.FapFormat.format(moneda1);
	}

	// Getter del atributo del tipo moneda
	public String getMoneda2_formatFapTabla() {
		return format.FapFormat.format(moneda2);
	}

	// Getter del atributo del tipo moneda
	public String getMoneda3_formatFapTabla() {
		return format.FapFormat.format(moneda3);
	}

	// Getter del atributo del tipo moneda
	public String getMoneda4_formatFapTabla() {
		return format.FapFormat.format(moneda4);
	}

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
