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
public class Cesion extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AutorizacionCesion autorizacionCesion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "cesion_cesiones")
	public List<Cesiones> cesiones;

	public Cesion() {
		init();
	}

	public void init() {

		if (autorizacionCesion == null)
			autorizacionCesion = new AutorizacionCesion();
		else
			autorizacionCesion.init();

		if (cesiones == null)
			cesiones = new ArrayList<Cesiones>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
