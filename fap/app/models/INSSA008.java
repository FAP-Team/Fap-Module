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
public class INSSA008 extends FapModel {
	// Código de los atributos

	public String cabeceraPrimera;

	public String cabeceraSegunda;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public RegistroDatosA008 registroDetalle;

	public INSSA008() {
		init();
	}

	public void init() {

		if (registroDetalle == null)
			registroDetalle = new RegistroDatosA008();
		else
			registroDetalle.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
