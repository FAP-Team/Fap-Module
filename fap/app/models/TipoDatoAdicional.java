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
public class TipoDatoAdicional extends Model {
	// Código de los atributos

	public Integer orden;

	public String nombre;

	@Column(columnDefinition = "LONGTEXT")
	public String descripcion;

	public void init() {

	}

	// === MANUAL REGION START ===
	public TipoDatoAdicional() {

	}

	public TipoDatoAdicional(int orden, String nombre, String descripcion) {
		this.orden = orden;
		this.nombre = nombre;
		this.descripcion = descripcion;
	}

	// === MANUAL REGION END ===

}