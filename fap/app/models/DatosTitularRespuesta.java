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
public class DatosTitularRespuesta extends FapModel {
	// Código de los atributos

	public String identificador;

	public String numeroSoporte;

	public String nombre;

	public String apellido1;

	public String apellido2;

	public String nacionalidad;

	public String nombrePadre;

	public String nombreMadre;

	public String fechacaducidad;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Sexo sexo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosNacimiento datosNacimiento;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosDireccion datosDireccion;

	public DatosTitularRespuesta() {
		init();
	}

	public void init() {

		if (sexo == null)
			sexo = new Sexo();
		else
			sexo.init();

		if (datosNacimiento == null)
			datosNacimiento = new DatosNacimiento();
		else
			datosNacimiento.init();

		if (datosDireccion == null)
			datosDireccion = new DatosDireccion();
		else
			datosDireccion.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
