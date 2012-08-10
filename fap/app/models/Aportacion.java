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
public class Aportacion extends FapModel {
	// Código de los atributos

	public String estado;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "aportacion_documentos")
	public List<Documento> documentos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public InformacionRegistro informacionRegistro;

	/* Cuando aportamos sin registro, se establece ésta fecha */

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaAportacionSinRegistro"), @Column(name = "fechaAportacionSinRegistroTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAportacionSinRegistro;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento borrador;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento oficial;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento justificante;

	public Aportacion() {
		init();
	}

	public void init() {

		if (documentos == null)
			documentos = new ArrayList<Documento>();

		if (informacionRegistro == null)
			informacionRegistro = new InformacionRegistro();
		else
			informacionRegistro.init();

		if (borrador == null)
			borrador = new Documento();
		else
			borrador.init();

		if (oficial == null)
			oficial = new Documento();
		else
			oficial.init();

		if (justificante == null)
			justificante = new Documento();
		else
			justificante.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
