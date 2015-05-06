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

/********** Entidades Respuesta - Peticion Sincrona  *************/

@Entity
public class TransmisionDatosRespuestaSVDFAP extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRespuesta"), @Column(name = "fechaRespuestaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRespuesta;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosGenericosRespuestaSVDFAP datosGenericos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosEspecificosRespuestaSVDFAP datosEspecificos;

	public TransmisionDatosRespuestaSVDFAP() {
		init();
	}

	public void init() {

		if (datosGenericos == null)
			datosGenericos = new DatosGenericosRespuestaSVDFAP();
		else
			datosGenericos.init();

		if (datosEspecificos == null)
			datosEspecificos = new DatosEspecificosRespuestaSVDFAP();
		else
			datosEspecificos.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
