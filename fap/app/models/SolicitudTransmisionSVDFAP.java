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
public class SolicitudTransmisionSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosGenericosPeticionSVDFAP datosGenericos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosEspecificosPeticionSVDFAP datosEspecificos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudGenerica solicitud;

	public SolicitudTransmisionSVDFAP() {
		init();
	}

	public void init() {

		if (datosGenericos == null)
			datosGenericos = new DatosGenericosPeticionSVDFAP();
		else
			datosGenericos.init();

		if (datosEspecificos == null)
			datosEspecificos = new DatosEspecificosPeticionSVDFAP();
		else
			datosEspecificos.init();

		if (solicitud == null)
			solicitud = new SolicitudGenerica();
		else
			solicitud.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	public SolicitudTransmisionSVDFAP(SolicitudGenerica solicitud, DatosGenericosPeticionSVDFAP datosGenericos, DatosEspecificosPeticionSVDFAP datosEspecificos) {
		init();
		this.setDatosGenericos(datosGenericos);
    	this.setDatosEspecificos(datosEspecificos);
    	this.setSolicitud(solicitud);
	}

	public DatosGenericosPeticionSVDFAP getDatosGenericos() {
		return datosGenericos;
	}

	public void setDatosGenericos(DatosGenericosPeticionSVDFAP datosGenericos) {
		this.datosGenericos = datosGenericos;
	}

	public DatosEspecificosPeticionSVDFAP getDatosEspecificos() {
		return datosEspecificos;
	}

	public void setDatosEspecificos(DatosEspecificosPeticionSVDFAP datosEspecificos) {
		this.datosEspecificos = datosEspecificos;
	}

	public SolicitudGenerica getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(SolicitudGenerica solicitud) {
		this.solicitud = solicitud;
	}
	
	
	// === MANUAL REGION END ===

}
