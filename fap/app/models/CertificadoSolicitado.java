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
public class CertificadoSolicitado extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TipoCertificado tipo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documento;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	public String estado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	public CertificadoSolicitado() {
		init();
	}

	public void init() {

		if (tipo == null)
			tipo = new TipoCertificado();
		else
			tipo.init();

		if (documento == null)
			documento = new Documento();
		else
			documento.init();

		if (registro == null)
			registro = new Registro();
		else
			registro.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
