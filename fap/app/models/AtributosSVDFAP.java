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
public class AtributosSVDFAP extends FapModel {
	// Código de los atributos

	public String idPeticion;

	public String codigoCertificado;

	public String timestamp;

	public Integer numElementos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoSVDFAP estado;

	public AtributosSVDFAP() {
		init();
	}

	public void init() {

		if (estado == null)
			estado = new EstadoSVDFAP();
		else
			estado.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public String getCodigoCertificado() {
		return codigoCertificado;
	}

	public void setCodigoCertificado(String codigoCertificado) {
		this.codigoCertificado = codigoCertificado;
	}

	public String getIdPeticion() {
		return idPeticion;
	}

	public void setIdPeticion(String idPeticion) {
		this.idPeticion = idPeticion;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getNumElementos() {
		return numElementos;
	}

	public void setNumElementos(Integer numElementos) {
		this.numElementos = numElementos;
	}

	// === MANUAL REGION END ===

}
