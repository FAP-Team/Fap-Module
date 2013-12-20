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
public class SolicitanteRespuesta extends FapModel {
	// Código de los atributos

	public String idSolicitante;

	public String nombreSolicitante;

	public String finalidad;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Consentimiento consentimiento;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public FuncionarioRespuesta funcionario;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ProcedimientoRespuesta procedimiento;

	public SolicitanteRespuesta() {
		init();
	}

	public void init() {

		if (consentimiento == null)
			consentimiento = new Consentimiento();
		else
			consentimiento.init();

		if (funcionario == null)
			funcionario = new FuncionarioRespuesta();
		else
			funcionario.init();

		if (procedimiento == null)
			procedimiento = new ProcedimientoRespuesta();
		else
			procedimiento.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
