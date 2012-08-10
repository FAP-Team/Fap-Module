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
public class ConfigurarMensaje extends FapModel {
	// Código de los atributos

	public String paginaAconfigurar;

	@ValueFromTable("tipoMensaje")
	public String tipoMensaje;

	public String tituloMensaje;

	@Column(columnDefinition = "LONGTEXT")
	public String contenido;

	public Boolean habilitar;

	@Transient
	public String habilitarText;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public String getHabilitarText() {
		if (habilitar)
			return "Sí";
		return "No";
	}

	// === MANUAL REGION END ===

}
