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
public class AutorizacionesFAP extends FapModel {
	// Código de los atributos

	@CheckWith(NipCheck.class)
	@Embedded
	public Nip nip;

	public AutorizacionesFAP() {
		init();
	}

	public void init() {

		if (nip == null)
			nip = new Nip();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
