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
public class Desistimiento extends FapModel {
	// Código de los atributos

	@Column(columnDefinition = "LONGTEXT")
	public String motivo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "desistimiento_documentos")
	public List<Documento> documentos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "desistimiento_documentosexternos")
	public List<DocumentoExterno> documentosExternos;

	public Desistimiento() {
		init();
	}

	public void init() {

		if (registro == null)
			registro = new Registro();
		else
			registro.init();

		if (documentos == null)
			documentos = new ArrayList<Documento>();

		if (documentosExternos == null)
			documentosExternos = new ArrayList<DocumentoExterno>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
