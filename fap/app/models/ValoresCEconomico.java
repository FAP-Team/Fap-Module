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

	public Integer anio;

	@Moneda
	public Double valorSolicitado;

	@Moneda
	public Double valorEstimado;

	@Moneda
	public Double valorPropuesto;

	@Moneda
	public Double valorConcedido;

	@Transient
	public String valorSolicitado_formatFapTabla;

	@Transient
	public String valorEstimado_formatFapTabla;

	@Transient
	public String valorPropuesto_formatFapTabla;

	@Transient
	public String valorConcedido_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getValorSolicitado_formatFapTabla() {
		return format.FapFormat.formatMoneda(valorSolicitado);
	}

	// Getter del atributo del tipo moneda
	public String getValorEstimado_formatFapTabla() {
		return format.FapFormat.formatMoneda(valorEstimado);
	}

	// Getter del atributo del tipo moneda
	public String getValorPropuesto_formatFapTabla() {
		return format.FapFormat.formatMoneda(valorPropuesto);
	}

	// Getter del atributo del tipo moneda
	public String getValorConcedido_formatFapTabla() {
		return format.FapFormat.formatMoneda(valorConcedido);
	}

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public ValoresCEconomico(int anio) {
		initValues(anio);
	}

	public ValoresCEconomico(ValoresCEconomico valoresCEconomico, int anio) {
		this.valorConcedido = valoresCEconomico.valorConcedido;
		this.valorEstimado = valoresCEconomico.valorEstimado;
		this.valorPropuesto = valoresCEconomico.valorPropuesto;
		this.valorSolicitado = valoresCEconomico.valorSolicitado;
		this.anio = anio;
	}

	public void initValues(int anioArg) {
		valorSolicitado = 0.0;
		valorEstimado = 0.0;
		valorPropuesto = 0.0;
		valorConcedido = 0.0;
		anio = anioArg;
	}
	// === MANUAL REGION END ===

}
