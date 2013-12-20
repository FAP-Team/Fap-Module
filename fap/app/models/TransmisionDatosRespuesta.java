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
public class TransmisionDatosRespuesta extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosGenericos datosGenericos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosEspecificos datosEspecificos;

	public TransmisionDatosRespuesta() {
		init();
	}

	public void init() {

		if (datosGenericos == null)
			datosGenericos = new DatosGenericos();
		else
			datosGenericos.init();

		if (datosEspecificos == null)
			datosEspecificos = new DatosEspecificos();
		else
			datosEspecificos.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
