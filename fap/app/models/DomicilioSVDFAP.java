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
public class DomicilioSVDFAP extends FapModel {
	// Código de los atributos

	@ValueFromTable("provincias")
	public String provincia;

	@ValueFromTable("municipios")
	public String municipio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadColectivaSVDFAP entColectiva;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadSingularSVDFAP entSingular;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EntidadNucleoSVDFAP nucleo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DireccionRespuestaSVDFAP direccion;

	public String codUnidadPoblacional;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public UltimaVariacionSVDFAP ultimaVariacion;

	public DomicilioSVDFAP() {
		init();
	}

	public void init() {

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

		if (ultimaVariacion == null)
			ultimaVariacion = new UltimaVariacionSVDFAP();
		else
			ultimaVariacion.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
