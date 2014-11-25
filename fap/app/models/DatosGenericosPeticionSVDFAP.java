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
public class DatosGenericosPeticionSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitanteSVDFAP solicitante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TitularSVDFAP titular;

	public DatosGenericosPeticionSVDFAP() {
		init();
	}

	public void init() {

		if (solicitante == null)
			solicitante = new SolicitanteSVDFAP();
		else
			solicitante.init();

		if (titular == null)
			titular = new TitularSVDFAP();
		else
			titular.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	public DatosGenericosPeticionSVDFAP(SolicitanteSVDFAP solicitante, TitularSVDFAP titular) {
		init();
		this.setSolicitante(solicitante);
    	this.setTitular(titular);
	}
	
	public SolicitanteSVDFAP getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(SolicitanteSVDFAP solicitante) {
		this.solicitante = solicitante;
	}

	public TitularSVDFAP getTitular() {
		return titular;
	}

	public void setTitular(TitularSVDFAP titular) {
		this.titular = titular;
	}

	// === MANUAL REGION END ===

}
