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
public class ResolucionFAP extends FapModel {
	// Código de los atributos

	@ValueFromTable("resolucionesDefinidas")
	public String tipoDefinidoResolucion;

	@ValueFromTable("modalidadResolucion")
	public String modalidad;

	@ValueFromTable("tipoResolucion")
	public String tipo;

	@ValueFromTable("estadoTipoMultiple")
	public String tipoMultiple;

	public Boolean firmarJefeServicio;

	public Boolean firmarDirector;

	public Boolean permitirPortafirma;

	public Boolean permitirRegistrar;

	public Boolean permitirPublicar;

	@ValueFromTable("estadoResolucion")
	public String estado;

	@Column(length = 2500)
	public String descripcion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "resolucionfap_lineasresolucion")
	public List<LineaResolucion> lineasResolucion;

	public Boolean conBaremacion;

	public Integer folio_inicio;

	public Integer folio_final;

	public Integer numero;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaResolucion"), @Column(name = "fechaResolucionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaResolucion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaIncioPreparacion"), @Column(name = "fechaIncioPreparacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaIncioPreparacion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "resolucionfap_docconsultaportafirmasresolucion")
	public List<Documento> docConsultaPortafirmasResolucion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	public ResolucionFAP() {
		init();
	}

	public void init() {

		if (lineasResolucion == null)
			lineasResolucion = new ArrayList<LineaResolucion>();

		if (conBaremacion == null)
			conBaremacion = false;

		if (docConsultaPortafirmasResolucion == null)
			docConsultaPortafirmasResolucion = new ArrayList<Documento>();

		if (registro == null)
			registro = new Registro();
		else
			registro.init();

		postInit();
	}

	// === MANUAL REGION START ===
	/**
	 * Calcula la lista de firmantes para la resolución
	 * @param agente
	 * @return
	 */
	public List<Firmante> calcularFirmantes(Agente agente) {

		return null;
	}
	// === MANUAL REGION END ===

}
