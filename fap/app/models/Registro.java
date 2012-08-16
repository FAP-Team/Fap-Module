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
public class Registro extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento borrador;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento oficial;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento justificante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento autorizacionFuncionario;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public InformacionRegistro informacionRegistro;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public FasesRegistro fasesRegistro;

	@ValueFromTable("tipoFirmaJuridica")
	public String tipoFirma;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Firmantes firmantes;

	public Registro() {
		init();
	}

	public void init() {

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

		if (autorizacionFuncionario == null)
			autorizacionFuncionario = new Documento();
		else
			autorizacionFuncionario.init();

		if (informacionRegistro == null)
			informacionRegistro = new InformacionRegistro();
		else
			informacionRegistro.init();

		if (fasesRegistro == null)
			fasesRegistro = new FasesRegistro();
		else
			fasesRegistro.init();

		if (firmantes == null)
			firmantes = new Firmantes();
		else
			firmantes.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
