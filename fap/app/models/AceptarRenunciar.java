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
public class AceptarRenunciar extends FapModel {
	// Código de los atributos

	@ValueFromTable("seleccion")
	public String seleccion;

	@Column(columnDefinition = "LONGTEXT")
	public String motivoRenuncia;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento borrador;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "aceptarrenunciar_documentos")
	public List<Documento> documentos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento justificante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	public AceptarRenunciar() {
		init();
	}

	public void init() {

		if (borrador == null)
			borrador = new Documento();
		else
			borrador.init();

		if (documentos == null)
			documentos = new ArrayList<Documento>();

		if (justificante == null)
			justificante = new Documento();
		else
			justificante.init();

		if (registro == null)
			registro = new Registro();
		else
			registro.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
