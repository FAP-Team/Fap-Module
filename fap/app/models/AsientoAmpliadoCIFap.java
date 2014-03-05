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
public class AsientoAmpliadoCIFap extends AsientoCIFap {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnUnidadOrganicaFap unidadOrganicaOrigen;

	public AsientoAmpliadoCIFap() {
		init();
	}

	public void init() {
		super.init();

		if (unidadOrganicaOrigen == null)
			unidadOrganicaOrigen = new ReturnUnidadOrganicaFap();
		else
			unidadOrganicaOrigen.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
