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

public class Expediente extends FapModel {
	// Código de los atributos

	public String idAed;

	public String idProcedimiento;

	public String valorModalidad;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaInicio"), @Column(name = "fechaInicioTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaInicio;

	public String estado;

	public String uri;

	@Transient
	public String verDocumentos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "expediente_documentos")
	public List<Documento> documentos;

	public Expediente() {
		init();
	}

	public void init() {

		if (documentos == null)
			documentos = new ArrayList<Documento>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
