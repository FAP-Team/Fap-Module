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
public class Exclusion extends FapModel {
	// Código de los atributos

	public String motivoExclusion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "exclusion_codigos")
	public List<CodigoExclusion> codigos;

	public Exclusion() {
		init();
	}

	public void init() {

		if (codigos == null)
			codigos = new ArrayList<CodigoExclusion>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
