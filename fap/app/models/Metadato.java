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
public class Metadato extends FapModel {
	// Código de los atributos

	public String nombre;

	public String valor;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public boolean esUnico() {
		return EsquemaMetadato.esUnico(this.nombre);
	}

	public boolean esObligatorio() {
		return EsquemaMetadato.esObligatorio(this.nombre);
	}

	
	public boolean esValido() {
		if (valor == null)
			return false;
		
		int longitud = Integer.MAX_VALUE;
		String longitudString = EsquemaMetadato.get(nombre).longitud;
		
		if (longitudString == null) {
			return true;
		}
		
		try {
			longitud = Integer.parseInt(longitudString);
		} catch (NumberFormatException e) {
			longitud = Integer.MAX_VALUE;
		}
		
		if (valor.length() <= longitud) {
			return true;
		}
		
		return false;
	}

	public boolean esAutomatizable() {
		//		TODO comprobar según el esquema
		return true;
	}

	public String getDescripcion() {
		//		TODO comprobar según el esquema
		return null;
	}

	// === MANUAL REGION END ===

}
