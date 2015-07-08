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
public class TitularSVDFAP extends FapModel {
	// Código de los atributos

	public String documentacion;

	public String nombreCompleto;

	public String nombre;

	public String apellido1;

	public String apellido2;

	@ValueFromTable("TipoDocumentacionSVDFAP")
	@FapEnum("enumerado.fap.gen.TipoDocumentacionSVDFAPEnum")
	public String tipoDocumentacion;

	public String identificador;

	public String numeroSoporte;

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

	public TitularSVDFAP() {
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

	public TitularSVDFAP(String nombre, String nombreCompleto, String apellido1, String apellido2, String nif, String tipoDocumentacion) {
		init();
		this.setDocumentacion(nif);
		this.setNombreCompleto(nombreCompleto);
		this.setNombre(nombre);
		this.setApellido1(apellido1);
		this.setApellido2(apellido2);
		this.setTipoDocumentacion(tipoDocumentacion);
	}

	public String getDocumentacion() {
		return documentacion;
	}

	public void setDocumentacion(String documentacion) {
		this.documentacion = documentacion;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido1() {
		return apellido1;
	}

	public void setApellido1(String apellido1) {
		this.apellido1 = apellido1;
	}

	public String getApellido2() {
		return apellido2;
	}

	public void setApellido2(String apellido2) {
		this.apellido2 = apellido2;
	}

	public String getTipoDocumentacion() {
		return tipoDocumentacion;
	}

	public void setTipoDocumentacion(String tipoDocumentacion) {
		this.tipoDocumentacion = tipoDocumentacion;
	}

	// === MANUAL REGION END ===

}
