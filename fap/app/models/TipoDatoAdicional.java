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

@Entity
public class TipoDatoAdicional extends FapModel {
	// Código de los atributos

	public Integer orden;

	public String nombre;

	@Column(columnDefinition = "LONGTEXT")
	public String descripcion;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	public TipoDatoAdicional() {

	}

	public TipoDatoAdicional(int orden, String nombre, String descripcion) {
		this.orden = orden;
		this.nombre = nombre;
		this.descripcion = descripcion;
	}

	public boolean esIgual(TipoDatoAdicional tipoDatoAdicional) {
		if (this.orden != null && this.orden.equals(tipoDatoAdicional.orden))
			return true;
		return false;
	}

	public void actualizar(TipoDatoAdicional tipoDatoAdicional) {
		this.orden = tipoDatoAdicional.orden;
		this.nombre = tipoDatoAdicional.nombre;
		this.descripcion = tipoDatoAdicional.descripcion;
	}

	public int estoyContenido(List<TipoDatoAdicional> lista) {
		for (TipoDatoAdicional busqueda : lista) {
			if (this.esIgual(busqueda))
				return lista.indexOf(busqueda);
		}
		return -1;
	}

	// === MANUAL REGION END ===

}
