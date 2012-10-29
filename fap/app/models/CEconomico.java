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
public class CEconomico extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TipoCEconomico tipo;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "ceconomico_valores")
	public List<ValoresCEconomico> valores;

	@Moneda
	@Transient
	public Double total;

	@Column(columnDefinition = "LONGTEXT")
	public String comentariosAdministracion;

	@Column(columnDefinition = "LONGTEXT")
	public String comentariosSolicitante;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "ceconomico_otros")
	public List<CEconomicosManuales> otros;

	@Transient
	public String total_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getTotal_formatFapTabla() {
		return format.FapFormat.formatMoneda(total);
	}

	public CEconomico() {
		init();
	}

	public void init() {

		if (tipo == null)
			tipo = new TipoCEconomico();
		else
			tipo.init();

		if (valores == null)
			valores = new ArrayList<ValoresCEconomico>();

		if (otros == null)
			otros = new ArrayList<CEconomicosManuales>();

		postInit();
	}

	// === MANUAL REGION START ===

	public CEconomico(CEconomico cEconomico) {
		this.tipo = new TipoCEconomico(cEconomico.tipo);
		this.valores = new ArrayList<ValoresCEconomico>();
		int orden = 0;
		for (ValoresCEconomico valoresCEconomico : cEconomico.valores) {
			this.valores.add(new ValoresCEconomico(valoresCEconomico, orden++));
		}
		this.comentariosAdministracion = cEconomico.comentariosAdministracion;
		this.comentariosSolicitante = cEconomico.comentariosSolicitante;
	}

	//
	public Double getTotal() {
		Double total = 0.0;
		for (ValoresCEconomico val : this.valores) {
			total += val.valorSolicitado==null? 0.0 : val.valorSolicitado;
		}
		return total;
	}
	// === MANUAL REGION END ===

}
