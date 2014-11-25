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
public class ResidenciaSVDFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ProvinciaSVDFAP provincia;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public MunicipioSVDFAP municipio;

	public ResidenciaSVDFAP() {
		init();
	}

	public void init() {

		if (provincia == null)
			provincia = new ProvinciaSVDFAP();
		else
			provincia.init();

		if (municipio == null)
			municipio = new MunicipioSVDFAP();
		else
			municipio.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	public ResidenciaSVDFAP(MunicipioSVDFAP municipio, ProvinciaSVDFAP provincia) {
		init();
		this.setMunicipio(municipio);
		this.setProvincia(provincia);
	}

	public ProvinciaSVDFAP getProvincia() {
		return provincia;
	}

	public void setProvincia(ProvinciaSVDFAP provincia) {
		this.provincia = provincia;
	}

	public MunicipioSVDFAP getMunicipio() {
		return municipio;
	}

	public void setMunicipio(MunicipioSVDFAP municipio) {
		this.municipio = municipio;
	}
	
	// === MANUAL REGION END ===

}
