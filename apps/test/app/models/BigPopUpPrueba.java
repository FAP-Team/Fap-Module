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
public class BigPopUpPrueba extends FapModel {
	// Código de los atributos

	public String prueba1;

	public String prueba2;

	public String prueba3;

	public String prueba4;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "bigpopupprueba_doc")
	public List<DocumentoExterno> doc;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "bigpopupprueba_doc2")
	public List<Documento> doc2;

	public boolean prueba5;

	public BigPopUpPrueba() {
		init();
	}

	public void init() {

		if (doc == null)
			doc = new ArrayList<DocumentoExterno>();

		if (doc2 == null)
			doc2 = new ArrayList<Documento>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
