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
public class Exclusion extends FapModel {
	// Código de los atributos

	public String motivoExclusion;

	@ElementCollection
	public List<String> codigosExclusionString;

	@Transient
	public List<TipoCodigoExclusion> codigosExclusion;

	public Exclusion() {
		init();
	}

	public void init() {

		if (codigosExclusion == null)
			codigosExclusion = new ArrayList<TipoCodigoExclusion>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
