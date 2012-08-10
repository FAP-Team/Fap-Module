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

@Entity
public class VisibilidadEstadoUsuario extends FapModel {
	// Código de los atributos

	public String estadoInterno;

	public String estadoUsuario;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
