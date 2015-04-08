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
public class HistoricoDeclaracionSubvenciones extends FapModel {
	// Código de los atributos

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "historicodeclaracionsubvenciones_declaraciones")
	public List<DeclaracionSubvenciones> declaraciones;

	public HistoricoDeclaracionSubvenciones() {
		init();
	}

	public void init() {

		if (declaraciones == null)
			declaraciones = new ArrayList<DeclaracionSubvenciones>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
