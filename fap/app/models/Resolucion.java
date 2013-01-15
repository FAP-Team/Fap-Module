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
public class Resolucion extends FapModel {
	// Código de los atributos

	@ValueFromTable("modalidadResolucion")
	public String modalidad;

	@ValueFromTable("tipoResolucion")
	public String tipo;

	@ValueFromTable("estadoTipoMultiple")
	public String tipoMultiple;

	@ValueFromTable("estadoResolucion")
	public String estado;

	@Column(length = 2500)
	public String descripcion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "resolucion_lineasresolucion")
	public List<LineaResolucion> lineasResolucion;

	public Boolean conBaremacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaIncioPreparacion"), @Column(name = "fechaIncioPreparacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaIncioPreparacion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "resolucion_docconsultaportafirmasresolucion")
	public List<Documento> docConsultaPortafirmasResolucion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	public Resolucion() {
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

	// === MANUAL REGION END ===

}
