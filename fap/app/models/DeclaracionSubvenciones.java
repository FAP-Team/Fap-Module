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
public class DeclaracionSubvenciones extends FapModel {
	// Código de los atributos

	public String nombreTramite;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "declaracionsubvenciones_subvenciones")
	public List<SubvencionFap> subvenciones;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fecha"), @Column(name = "fechaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;

	@Transient
	public Double totalSolicitadas;

	@Transient
	public Double totalConcedidas;

	@Transient
	public Double totalSolicitadasPublicas;

	@Transient
	public Double totalConcedidasPublicas;

	@Transient
	public Double totalSolicitadasPrivadas;

	@Transient
	public Double totalConcedidasPrivadas;

	@Transient
	public Double totalSolicitadasMinimis;

	@Transient
	public Double totalConcedidasMinimis;

	public DeclaracionSubvenciones() {
		init();
	}

	public void init() {

		if (subvenciones == null)
			subvenciones = new ArrayList<SubvencionFap>();

		postInit();
	}

	// === MANUAL REGION START ===

	private Double obtenerTotalPorTipoYEstado(String tipo, String estado) {
		double total = 0.0;
		for (SubvencionFap sub : this.subvenciones) {
			if (sub.situacion.equals(estado) && sub.tipo.equals(tipo))
				total += sub.importe;
		}
		return total;
	}

	public Double getTotalSolicitadasPublicas() {
		return obtenerTotalPorTipoYEstado("publica", "solicitada");
	}

	public Double getTotalConcedidasPublicas() {
		return obtenerTotalPorTipoYEstado("publica", "concedida");
	}

	public Double getTotalSolicitadasPrivadas() {
		return obtenerTotalPorTipoYEstado("privada", "solicitada");
	}

	public Double getTotalConcedidasPrivadas() {
		return obtenerTotalPorTipoYEstado("privada", "concedida");
	}

	public Double getTotalSolicitadasMinimis() {
		return obtenerTotalPorTipoYEstado("minimis", "solicitada");
	}

	public Double getTotalConcedidasMinimis() {
		return obtenerTotalPorTipoYEstado("minimis", "concedida");
	}

	public Double getTotalSolicitadas() {
		return this.totalSolicitadasPublicas + this.totalSolicitadasPrivadas + this.totalSolicitadasMinimis;
	}

	public Double getTotalConcedidas() {
		return this.totalConcedidasPublicas + this.totalConcedidasPrivadas + this.totalConcedidasMinimis;
	}

	private List<SubvencionFap> obtenerSubvencionesTipo(String tipo) {
		List<SubvencionFap> lst = new ArrayList<SubvencionFap>();
		if (this.subvenciones.isEmpty())
			return null;
		for (SubvencionFap sub : this.subvenciones) {
			if (sub.tipo.equals(tipo))
				lst.add(sub);
		}
		return lst;
	}

	public List<SubvencionFap> obtenerSubvencionesPublicas() {
		List<SubvencionFap> lst = obtenerSubvencionesTipo("publica");
		return lst;
	}

	public List<SubvencionFap> obtenerSubvencionesMinimis() {
		List<SubvencionFap> lst = obtenerSubvencionesTipo("minimis");
		return lst;
	}

	public List<SubvencionFap> obtenerSubvencionesPrivadas() {
		List<SubvencionFap> lst = obtenerSubvencionesTipo("privada");
		return lst;
	}

	// === MANUAL REGION END ===

}
