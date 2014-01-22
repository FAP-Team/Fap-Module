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
public class DomicilioRespuesta extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Provincia provincia;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Municipio municipio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadColectiva entColectiva;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadSingular entSingular;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadNucleo nucleo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DireccionRespuesta direccion;

	public DomicilioRespuesta() {
		init();
	}

	public void init() {

		if (provincia == null)
			provincia = new Provincia();
		else
			provincia.init();

		if (municipio == null)
			municipio = new Municipio();
		else
			municipio.init();

		if (entColectiva == null)
			entColectiva = new EntidadColectiva();
		else
			entColectiva.init();

		if (entSingular == null)
			entSingular = new EntidadSingular();
		else
			entSingular.init();

		if (nucleo == null)
			nucleo = new EntidadNucleo();
		else
			nucleo.init();

		if (direccion == null)
			direccion = new DireccionRespuesta();
		else
			direccion.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
