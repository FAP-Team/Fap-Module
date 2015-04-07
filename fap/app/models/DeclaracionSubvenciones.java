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

	@Moneda
	@Transient
	public Double totalSolicitadas;

	@Moneda
	@Transient
	public Double totalConcedidas;

	@Moneda
	@Transient
	public Double totalSolicitadasPublicas;

	@Moneda
	@Transient
	public Double totalConcedidasPublicas;

	@Moneda
	@Transient
	public Double totalSolicitadasPrivadas;

	@Moneda
	@Transient
	public Double totalConcedidasPrivadas;

	@Moneda
	@Transient
	public Double totalSolicitadasMinimis;

	@Moneda
	@Transient
	public Double totalConcedidasMinimis;

	@Transient
	public String totalSolicitadas_formatFapTabla;

	@Transient
	public String totalConcedidas_formatFapTabla;

	@Transient
	public String totalSolicitadasPublicas_formatFapTabla;

	@Transient
	public String totalConcedidasPublicas_formatFapTabla;

	@Transient
	public String totalSolicitadasPrivadas_formatFapTabla;

	@Transient
	public String totalConcedidasPrivadas_formatFapTabla;

	@Transient
	public String totalSolicitadasMinimis_formatFapTabla;

	@Transient
	public String totalConcedidasMinimis_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getTotalSolicitadas_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalSolicitadas);
	}

	// Getter del atributo del tipo moneda
	public String getTotalConcedidas_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalConcedidas);
	}

	// Getter del atributo del tipo moneda
	public String getTotalSolicitadasPublicas_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalSolicitadasPublicas);
	}

	// Getter del atributo del tipo moneda
	public String getTotalConcedidasPublicas_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalConcedidasPublicas);
	}

	// Getter del atributo del tipo moneda
	public String getTotalSolicitadasPrivadas_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalSolicitadasPrivadas);
	}

	// Getter del atributo del tipo moneda
	public String getTotalConcedidasPrivadas_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalConcedidasPrivadas);
	}

	// Getter del atributo del tipo moneda
	public String getTotalSolicitadasMinimis_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalSolicitadasMinimis);
	}

	// Getter del atributo del tipo moneda
	public String getTotalConcedidasMinimis_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalConcedidasMinimis);
	}

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

	// === MANUAL REGION END ===

}
