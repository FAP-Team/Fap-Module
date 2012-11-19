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
public class FirmaEnServidor extends FapModel {
	// Código de los atributos

	public Boolean fueGenerado;

	public Boolean fueFirmado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento oficial;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento firmado;

	public FirmaEnServidor() {
		init();
	}

	public void init() {

		if (oficial == null)
			oficial = new Documento();
		else
			oficial.init();

		if (firmado == null)
			firmado = new Documento();
		else
			firmado.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
