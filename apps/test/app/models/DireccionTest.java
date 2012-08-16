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
public class DireccionTest extends FapModel {
	// Código de los atributos

	@Embedded
	public Direccion direccion;

	@Embedded
	public DireccionMapa direccionMapa;

	public DireccionTest() {
		init();
	}

	public void init() {

		if (direccion == null)
			direccion = new Direccion();

		if (direccionMapa == null)
			direccionMapa = new DireccionMapa();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
