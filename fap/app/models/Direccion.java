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
public class Direccion {
	// Código de los atributos

	public String calle;

	public String numero;

	public String otros;

	public String codigoPostal;

	@ValueFromTable("municipios")
	public String municipio;

	@ValueFromTable("islas")
	public String isla;

	@ValueFromTable("provincias")
	public String provincia;

	@ValueFromTable("comunidadesAutonomas")
	public String comunidad;

	@ValueFromTable("paises")
	public String pais;

	public String provinciaInternacional;

	public String localidad;

	@ValueFromTable("tipoDireccion")
	public String tipo;

	public Direccion() {
		init();
	}

	public void init() {

		if (calle == null)
			calle = new String();
		if (numero == null)
			numero = new String();
		if (otros == null)
			otros = new String();
		if (codigoPostal == null)
			codigoPostal = new String();
		if (provinciaInternacional == null)
			provinciaInternacional = new String();
		if (localidad == null)
			localidad = new String();

	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
