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
public class TablaPaginas_nivel1 extends FapModel {
	// Código de los atributos

	public String nombre;

	public Integer numero;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tablapaginas_nivel1_combomul")
	public List<ComboTestRef> comboMul;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fecha"), @Column(name = "fechaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tablapaginas_nivel1_tpaginas_nivel2")
	public List<TablaPaginas_nivel2> tpaginas_nivel2;

	public TablaPaginas_nivel1() {
		init();
	}

	public void init() {

		if (comboMul == null)
			comboMul = new ArrayList<ComboTestRef>();

		if (tpaginas_nivel2 == null)
			tpaginas_nivel2 = new ArrayList<TablaPaginas_nivel2>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
