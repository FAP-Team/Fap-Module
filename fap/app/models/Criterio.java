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
public class Criterio extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TipoCriterio tipo;

	public Double valor;

	@Column(columnDefinition = "LONGTEXT")
	public String comentariosAdministracion;

	@Column(columnDefinition = "LONGTEXT")
	public String comentariosSolicitante;

	public Criterio() {
		init();
	}

	public void init() {

		if (tipo == null)
			tipo = new TipoCriterio();
		else
			tipo.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
