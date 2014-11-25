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
public class DatosEspecificosPeticionSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitanteDatosSVDFAP solicitanteDatos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudSVDFAP solicitud;

	public DatosEspecificosPeticionSVDFAP() {
		init();
	}

	public void init() {

		if (solicitanteDatos == null)
			solicitanteDatos = new SolicitanteDatosSVDFAP();
		else
			solicitanteDatos.init();

		if (solicitud == null)
			solicitud = new SolicitudSVDFAP();
		else
			solicitud.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	public DatosEspecificosPeticionSVDFAP(SolicitanteDatosSVDFAP solicitanteDatos, SolicitudSVDFAP solicitudEspecifica) {
		init();
		this.setSolicitanteDatos(solicitanteDatos);
    	this.setSolicitud(solicitudEspecifica);
	}

	public SolicitanteDatosSVDFAP getSolicitanteDatos() {
		return solicitanteDatos;
	}

	public void setSolicitanteDatos(SolicitanteDatosSVDFAP solicitanteDatos) {
		this.solicitanteDatos = solicitanteDatos;
	}

	public SolicitudSVDFAP getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(SolicitudSVDFAP solicitud) {
		this.solicitud = solicitud;
	}

	
	// === MANUAL REGION END ===

}
