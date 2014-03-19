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
public class ReturnInteresadoFap extends ReturnInteresadoCIFap {
	// Código de los atributos

	@ValueFromTable("tipoDocumentoCI")
	public String tipoDocumento;

	public String numeroDocumento;

	public String letra;

	public void init() {
		super.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
