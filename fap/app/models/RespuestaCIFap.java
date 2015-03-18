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
public class RespuestaCIFap extends FapModel {
	// Código de los atributos

	public String usuario;

	public String resumen;

	public String observaciones;

	public String fecha;

	public String hora;

	public String tipoComunicacion;

	public String ejercicio;

	public Long numeroGeneral;

	public String contadorUO;

	public Long numeroRegistro;

	public String asunto;

	public String unidadOrganica;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnInteresadoCIFap interesado;

	public String tipoTransporte;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "respuestacifap_uris")
	public List<ListaUris> uris;

	public String unidadOrganicaOrigen;

	public String unidadOrganicaPropuesta;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnErrorFap error;

	public RespuestaCIFap() {
		init();
	}

	public void init() {

		if (uris == null)
			uris = new ArrayList<ListaUris>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
