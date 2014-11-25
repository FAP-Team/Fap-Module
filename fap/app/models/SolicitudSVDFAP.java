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
public class SolicitudSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ResidenciaSVDFAP residencia;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public NacimientoSVDFAP solicitudNacimiento;

	public String espanol;

	public SolicitudSVDFAP() {
		init();
	}

	public void init() {

		if (residencia == null)
			residencia = new ResidenciaSVDFAP();
		else
			residencia.init();

		if (solicitudNacimiento == null)
			solicitudNacimiento = new NacimientoSVDFAP();
		else
			solicitudNacimiento.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	public SolicitudSVDFAP(ResidenciaSVDFAP residencia, NacimientoSVDFAP nacimiento, String espaniol) {
		init();
		this.setResidencia(residencia);
    	this.setSolicitudNacimiento(nacimiento);
		this.setEspanol(espaniol);
	}
	
	public ResidenciaSVDFAP getResidencia() {
		return residencia;
	}

	public void setResidencia(ResidenciaSVDFAP residencia) {
		this.residencia = residencia;
	}

	public NacimientoSVDFAP getSolicitudNacimiento() {
		return solicitudNacimiento;
	}

	public void setSolicitudNacimiento(NacimientoSVDFAP solicitudNacimiento) {
		this.solicitudNacimiento = solicitudNacimiento;
	}

	public String getEspanol() {
		return espanol;
	}

	public void setEspanol(String espanol) {
		this.espanol = espanol;
	}

	// === MANUAL REGION END ===

}
