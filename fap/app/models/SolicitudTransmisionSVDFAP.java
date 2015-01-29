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

/******* Solicitud de Transmision *******/

@Entity
public class SolicitudTransmisionSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosGenericosPeticionSVDFAP datosGenericos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosEspecificosSVDFAP datosEspecificos;

	@ManyToOne(fetch = FetchType.LAZY)
	public SolicitudGenerica solicitud;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TransmisionDatosRespuestaSVDFAP respuesta;

	@ValueFromTable("NombreServicioSVDFAP")
	public String nombreServicio;

	public String estado;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	public SolicitudTransmisionSVDFAP() {
		init();
	}

	public void init() {

		if (datosGenericos == null)
			datosGenericos = new DatosGenericosPeticionSVDFAP();
		else
			datosGenericos.init();

		if (datosEspecificos == null)
			datosEspecificos = new DatosEspecificosSVDFAP();
		else
			datosEspecificos.init();

		if (solicitud != null)
			solicitud.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public SolicitudTransmisionSVDFAP(SolicitudGenerica solicitud, DatosGenericosPeticionSVDFAP datosGenericos, DatosEspecificosSVDFAP datosEspecificos) {
		init();
		this.datosGenericos = datosGenericos;
		this.datosEspecificos = datosEspecificos;
		this.solicitud = solicitud;
	}

	@Override
	public void postInit() {

		fechaCreacion = new DateTime();
		estado = "creada";

	}

	// === MANUAL REGION END ===

}
