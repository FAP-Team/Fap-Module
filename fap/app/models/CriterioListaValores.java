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
public class CriterioListaValores extends FapModel {
	// Código de los atributos

	@Moneda
	public Double valor;

	public String descripcion;

	@Transient
	public String valor_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getValor_formatFapTabla() {
		return format.FapFormat.formatMoneda(valor);
	}

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
