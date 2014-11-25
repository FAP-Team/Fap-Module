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
public class ProvinciaSVDFAP extends FapModel {
	// Código de los atributos

	public String codigo;

	public String nombre;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	
	public ProvinciaSVDFAP(){
		init();
	}
	
	public ProvinciaSVDFAP(String codigo, String nombre){
		init();
		this.setCodigo(codigo);
    	this.setNombre(nombre);
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	// === MANUAL REGION END ===

}
