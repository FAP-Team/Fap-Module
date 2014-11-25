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

	@Transient
	public String nombreCompleto;

	public String nombre;

	public String apellido1;

	public String apellido2;

	@ValueFromTable("TipoDocumentacion")
	public String tipoDocumentacion;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	
	public TitularSVDFAP(){
		init();
	}
	
	public TitularSVDFAP(String nombre, String nombreCompleto, String apellido1, String apellido2, String nif, String tipoDocumentacion){
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
