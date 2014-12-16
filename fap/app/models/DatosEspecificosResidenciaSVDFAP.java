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

/**** Servicio Residencia **/

@Entity
public class DatosEspecificosResidenciaSVDFAP extends DatosEspecificosSVDFAP {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoSVDFAP estado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoResultadoSVDFAP estadoResultado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DomicilioRespuestaSVDFAP domicilio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosTitularRespuestaSVDFAP datosTitular;

	public DatosEspecificosResidenciaSVDFAP() {
		init();
	}

	public void init() {
		super.init();

		if (estado == null)
			estado = new EstadoSVDFAP();
		else
			estado.init();

		if (estadoResultado == null)
			estadoResultado = new EstadoResultadoSVDFAP();
		else
			estadoResultado.init();

		if (domicilio == null)
			domicilio = new DomicilioRespuestaSVDFAP();
		else
			domicilio.init();

		if (datosTitular == null)
			datosTitular = new DatosTitularRespuestaSVDFAP();
		else
			datosTitular.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
