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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import format.FapFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

public class BusquedaLogs extends FapModel {
	// Código de los atributos

	public Boolean buquedaPorAtributos;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaLog"), @Column(name = "fechaLogTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaLog;

	public String tipoLog;

	public String mensajeLog;

	public String usuario;

	public String claseLog;

	public String numeroFilasSeleccionadas;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
