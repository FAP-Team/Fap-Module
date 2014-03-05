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
public class SolicitantePeticion extends FapModel {
	// Código de los atributos

	public String identificadorSolicitante;

	public String nombreSolicitante;

	public String finalidad;

	public String idExpediente;

	public String unidadTramitadora;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Procedimiento procedimiento;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Funcionario funcionario;

	@ValueFromTable("TipoConsentimiento")
	public String consentimiento;

	public SolicitantePeticion() {
		init();
	}

	public void init() {

		if (procedimiento == null)
			procedimiento = new Procedimiento();
		else
			procedimiento.init();

		if (funcionario == null)
			funcionario = new Funcionario();
		else
			funcionario.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
