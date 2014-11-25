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
public class NacimientoSVDFAP extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fecha"), @Column(name = "fechaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public MunicipioSVDFAP municipio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ProvinciaSVDFAP provincia;

	public NacimientoSVDFAP() {
		init();
	}

	public void init() {

		if (municipio == null)
			municipio = new MunicipioSVDFAP();
		else
			municipio.init();

		if (provincia == null)
			provincia = new ProvinciaSVDFAP();
		else
			provincia.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	public NacimientoSVDFAP(DateTime fecha, MunicipioSVDFAP municipio, ProvinciaSVDFAP provincia) {
		init();
		this.setFecha(fecha);
		this.setMunicipio(municipio);
		this.setProvincia(provincia);
	}

	public DateTime getFecha() {
		return fecha;
	}

	public void setFecha(DateTime fecha) {
		this.fecha = fecha;
	}

	public MunicipioSVDFAP getMunicipio() {
		return municipio;
	}

	public void setMunicipio(MunicipioSVDFAP municipio) {
		this.municipio = municipio;
	}

	public ProvinciaSVDFAP getProvincia() {
		return provincia;
	}

	public void setProvincia(ProvinciaSVDFAP provincia) {
		this.provincia = provincia;
	}
	
	// === MANUAL REGION END ===

}
