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
public class TablaDeNombres extends FapModel {
	// Código de los atributos

	public String nombre;

	public String apellido;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Fechas fecha;

	public TablaDeNombres() {
		init();
	}

	public void init() {

		if (fecha == null)
			fecha = new Fechas();
		else
			fecha.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
