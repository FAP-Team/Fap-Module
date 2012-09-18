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
		if (numero == null)
			numero = new String();
		if (otros == null)
			otros = new String();
		if (calle == null)
			calle = new String();
		if (localidad == null)
			localidad = new String();
		if (codigoPostal == null)
			codigoPostal = new String();
		if (municipio == null)
			municipio = new String();
		if (provincia == null)
			provincia = new String();
		if (comunidad == null)
			comunidad = new String();
		if (pais == null)
			pais = new String();

	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
