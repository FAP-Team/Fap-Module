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
public class ValoresCEconomico extends FapModel {
	// Código de los atributos

	public Double valorSolicitado;

	public Double valorEstimado;

	public Double valorPropuesto;

	public Double valorConcedido;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public ValoresCEconomico() {
		initValues();
	}

	public ValoresCEconomico(ValoresCEconomico valoresCEconomico) {
		this.valorConcedido = valoresCEconomico.valorConcedido;
		this.valorEstimado = valoresCEconomico.valorEstimado;
		this.valorPropuesto = valoresCEconomico.valorPropuesto;
		this.valorSolicitado = valoresCEconomico.valorSolicitado;
	}

	public void initValues() {
		valorSolicitado = 0.0;
		valorEstimado = 0.0;
		valorPropuesto = 0.0;
		valorConcedido = 0.0;
	}
	// === MANUAL REGION END ===

}
