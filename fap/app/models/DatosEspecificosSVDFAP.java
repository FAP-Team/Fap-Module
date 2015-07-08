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
public class DatosEspecificosSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosEspecificosIdResiSVDFAP datosEspecificosIdResi;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoSVDFAP estado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoSVDFAP estadoResultado;

	public DatosEspecificosSVDFAP() {
		init();
	}

	public void init() {

		if (datosEspecificosIdResi == null)
			datosEspecificosIdResi = new DatosEspecificosIdResiSVDFAP();
		else
			datosEspecificosIdResi.init();

		if (estado == null)
			estado = new EstadoSVDFAP();
		else
			estado.init();

		if (estadoResultado == null)
			estadoResultado = new EstadoSVDFAP();
		else
			estadoResultado.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
