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
import properties.FapProperties;

// === IMPORT REGION END ===

@Entity
public class Propiedades extends FapModel {
	// Código de los atributos

	public String descripcion;

	public String clave;

	@Transient
	public String valor;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public String getValor() {
		return FapProperties.get(clave);
	}

	public void setValor(String valor) {
	}

	// === MANUAL REGION END ===

}
