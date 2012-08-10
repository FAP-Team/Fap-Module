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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import format.FapFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Embeddable
public class DireccionMapa {
	// Código de los atributos

	public String direccionBusqueda;

	public Double latitud;

	public Double longitud;

	public String numero;

	public String otros;

	public String calle;

	public String localidad;

	public String codigoPostal;

	public String municipio;

	public String provincia;

	public String comunidad;

	public String pais;

	public DireccionMapa() {
		init();
	}

	public void init() {

		if (direccionBusqueda == null)
			direccionBusqueda = new String();

	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
