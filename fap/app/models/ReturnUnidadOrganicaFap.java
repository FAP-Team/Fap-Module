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
public class ReturnUnidadOrganicaFap extends FapModel {
	// Código de los atributos

	public Long codigo;

	public String codigoCompleto;

	public String descripcion;

	public String esBaja;

	public String esReceptora;

	public Long codigoReceptora;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnErrorFap error;

	@Transient
	public Long codigoRaiz;

	@Transient
	public Long codigoSubNivel1;

	@Transient
	public Long codigoSubNivel2;

	@Transient
	public Long codigoSubNivel3;

	public ReturnUnidadOrganicaFap() {
		init();
	}

	public void init() {

		if (error == null)
			error = new ReturnErrorFap();
		else
			error.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
