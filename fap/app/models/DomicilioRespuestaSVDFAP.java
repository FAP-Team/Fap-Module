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
public class DomicilioRespuestaSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ProvinciaSVDFAP provincia;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public MunicipioSVDFAP municipio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadColectivaSVDFAP entColectiva;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadSingularSVDFAP entSingular;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadNucleoSVDFAP nucleo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DireccionRespuestaSVDFAP direccion;

	public DomicilioRespuestaSVDFAP() {
		init();
	}

	public void init() {

		if (provincia == null)
			provincia = new ProvinciaSVDFAP();
		else
			provincia.init();

		if (municipio == null)
			municipio = new MunicipioSVDFAP();
		else
			municipio.init();

		if (entColectiva == null)
			entColectiva = new EntidadColectivaSVDFAP();
		else
			entColectiva.init();

		if (entSingular == null)
			entSingular = new EntidadSingularSVDFAP();
		else
			entSingular.init();

		if (nucleo == null)
			nucleo = new EntidadNucleoSVDFAP();
		else
			nucleo.init();

		if (direccion == null)
			direccion = new DireccionRespuestaSVDFAP();
		else
			direccion.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
