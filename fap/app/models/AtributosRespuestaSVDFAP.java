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
public class AtributosRespuestaSVDFAP extends FapModel {
	// Código de los atributos

	public String idPeticion;

	public String codigoCertificado;

	public String timestamp;

	public Integer numElementos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoRespuestaSVDFAP estado;

	public AtributosRespuestaSVDFAP() {
		init();
	}

	public void init() {

		if (estado == null)
			estado = new EstadoRespuestaSVDFAP();
		else
			estado.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
