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
public class DatosTitularRespuestaSVDFAP extends FapModel {
	// Código de los atributos

	public String identificador;

	public String numeroSoporte;

	public String nombre;

	public String apellido1;

	public String apellido2;

	public String nacionalidad;

	public String nombrePadre;

	public String nombreMadre;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechacaducidad"), @Column(name = "fechacaducidadTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechacaducidad;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SexoSVDFAP sexo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public NacimientoSVDFAP datosNacimiento;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosDireccionSVDFAP datosDireccion;

	public DatosTitularRespuestaSVDFAP() {
		init();
	}

	public void init() {

		if (sexo == null)
			sexo = new SexoSVDFAP();
		else
			sexo.init();

		if (datosNacimiento == null)
			datosNacimiento = new NacimientoSVDFAP();
		else
			datosNacimiento.init();

		if (datosDireccion == null)
			datosDireccion = new DatosDireccionSVDFAP();
		else
			datosDireccion.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
