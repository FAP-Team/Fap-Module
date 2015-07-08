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

import services.verificacionDatos.SVDUtils;

// === IMPORT REGION END ===

/******* Solicitud de Transmision *******/

@Entity
public class SolicitudTransmisionSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosGenericosSVDFAP datosGenericos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosEspecificosSVDFAP datosEspecificos;

	@ManyToOne(fetch = FetchType.LAZY)
	public SolicitudGenerica solicitud;

	@ValueFromTable("NombreServicioSVDFAP")
	@FapEnum("enumerado.fap.gen.NombreServicioSVDFAPEnum")
	public String nombreServicio;

	public String estado;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaPeticion"), @Column(name = "fechaPeticionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaPeticion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRespuesta"), @Column(name = "fechaRespuestaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRespuesta;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento justificanteSVD;

	public SolicitudTransmisionSVDFAP() {
		init();
	}

	public void init() {

		if (datosGenericos == null)
			datosGenericos = new DatosGenericosSVDFAP();
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

	public SolicitudTransmisionSVDFAP(SolicitudGenerica solicitud, DatosGenericosSVDFAP datosGenericos, DatosEspecificosSVDFAP datosEspecificos) {
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
