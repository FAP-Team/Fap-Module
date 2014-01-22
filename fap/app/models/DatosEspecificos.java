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
public class DatosEspecificos extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoDatosEspecificosRespuesta estado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoResultadoRespuesta estadoResultado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DomicilioRespuesta domicilio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosTitularRespuesta datosTitular;

	public DatosEspecificos() {
		init();
	}

	public void init() {

		if (estado == null)
			estado = new EstadoDatosEspecificosRespuesta();
		else
			estado.init();

		if (estadoResultado == null)
			estadoResultado = new EstadoResultadoRespuesta();
		else
			estadoResultado.init();

		if (domicilio == null)
			domicilio = new DomicilioRespuesta();
		else
			domicilio.init();

		if (datosTitular == null)
			datosTitular = new DatosTitularRespuesta();
		else
			datosTitular.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
