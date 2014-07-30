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
import org.hibernate.annotations.DiscriminatorOptions;
import properties.FapProperties;

@DiscriminatorOptions(force = true)
// === IMPORT REGION END ===
@Entity
public class Convocatoria extends Singleton {
	// Código de los atributos

	@ValueFromTable("estadoConvocatoria")
	public String estado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ExpedienteAed expedienteAed;

	public Convocatoria() {
		init();
	}

	public void init() {
		super.init();
		if (estado == null)
			estado = "presentacion";

		if (expedienteAed == null)
			expedienteAed = new ExpedienteAed();
		else
			expedienteAed.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public static String getModalidadConvocatoria() {
		return FapProperties.get("fap.aed.expediente.modalidad");
	}

	// === MANUAL REGION END ===

}
