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
public class ReturnComunicacionInternaFap extends FapModel {
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
	@JoinTable(name = "returncomunicacioninternafap_uris")
	public List<ListaUris> uris;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnErrorFap error;

	public ReturnComunicacionInternaFap() {
		init();
	}

	public void init() {

		if (interesado == null)
			interesado = new ReturnInteresadoCIFap();
		else
			interesado.init();

		if (uris == null)
			uris = new ArrayList<ListaUris>();

		if (error == null)
			error = new ReturnErrorFap();
		else
			error.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
