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
public class ConvocatoriaApp extends Convocatoria {
	// Código de los atributos

	public String codigoSefcan;

	@Column(length = 500)
	public String nombreConvocatoria;

	/*Número de documento contable RC pueden haber varios se mantendrá el último*/

	public String numeroRc;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRc"), @Column(name = "fechaRcTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRc;

	/*Fecha de contabilización RC */

	@Column(columnDefinition = "LONGTEXT")
	public String observaciones;

	public void init() {
		super.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
