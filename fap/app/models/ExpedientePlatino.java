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
import properties.FapProperties;

// === IMPORT REGION END ===

@Auditable
@Entity
public class ExpedientePlatino extends Model {
	// Código de los atributos

	public String uri;

	public String numero;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaApertura"), @Column(name = "fechaAperturaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaApertura;

	public Boolean creado;

	public String ruta;

	public void init() {

	}

	// === MANUAL REGION START ===

	public Boolean getCreado() {
		if (creado == null)
			return false;
		return creado;
	}

	public String getNumero() {
		if (numero == null) {
			numero = UUID.randomUUID().toString();
			save();
		}
		return numero;
	}

	public DateTime getFechaApertura() {
		if (fechaApertura == null) {
			fechaApertura = new DateTime();
			save();
		}
		return fechaApertura;
	}

	public String getRuta() {
		if (ruta == null) {
			String procedimiento = FapProperties.get("fap.platino.gestordocumental.procedimiento");
			ruta = "expedientes/" + procedimiento + "/" + fechaApertura.getYear() + "/" + getNumero();
			save();
		}
		return ruta;
	}

	// === MANUAL REGION END ===

}
