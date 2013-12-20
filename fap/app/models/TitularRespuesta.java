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
public class TitularRespuesta extends FapModel {
	// Código de los atributos

	public String documentacion;

	public String apellido1;

	public String apellido2;

	public String nombreCompleto;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TipoDocumentacion tipoDocumentacion;

	public TitularRespuesta() {
		init();
	}

	public void init() {

		if (tipoDocumentacion == null)
			tipoDocumentacion = new TipoDocumentacion();
		else
			tipoDocumentacion.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
