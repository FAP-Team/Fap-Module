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

@Auditable
@Entity
public class InformacionRegistro extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRegistro"), @Column(name = "fechaRegistroTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;

	public String unidadOrganica;

	public String numeroRegistro;

	public String numeroRegistroGeneral;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	public void setDataFromJustificante(models.JustificanteRegistro justificante) {
		fechaRegistro = justificante.getFechaRegistro();
		unidadOrganica = justificante.getUnidadOrganica();
		numeroRegistro = justificante.getNumeroRegistro();
		numeroRegistroGeneral = justificante.getNumeroRegistroGeneral();
		save();
	}
	// === MANUAL REGION END ===

}
