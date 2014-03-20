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
public class EsquemaMetadato extends FapModel {
	// Código de los atributos

	public String identificador;

	public String nombre;

	@Column(columnDefinition = "LONGTEXT")
	public String definicion;

	public String obligatoriedad;

	public String tipoDeDato;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "esquemametadato_valores")
	public List<ValoresValidosMetadatos> valores;

	public String longitud;

	@ElementCollection
	public List<String> patron;

	public String repeticion;

	@Column(columnDefinition = "LONGTEXT")
	public String comentario;

	public String equivalencia;

	public String automatizable;

	public EsquemaMetadato() {
		init();
	}

	public void init() {

		if (valores == null)
			valores = new ArrayList<ValoresValidosMetadatos>();

		postInit();
	}

	// === MANUAL REGION START ===
	public static EsquemaMetadato get(String nombre) {
		EsquemaMetadato esq = find("byNombre", nombre).first();
		return esq;
	}
	
	public boolean esObligatorio() {
		if ("si".equals(obligatoriedad)) {
			return true;
		}
		return false;
	}
	
	public static boolean esObligatorio(String metadatoNombre) {
		EsquemaMetadato esq = get(metadatoNombre);
		if (esq == null) {
			return false;
		}
		return esq.esObligatorio();
	}
	
	
	public boolean esUnico() {
		if ("unico".equals(repeticion)) {
			return true;
		}
		return false;
	}
	
	public static boolean esUnico(String nombre) {
		EsquemaMetadato esq = find("byNombre", nombre).first();
		return esq.esUnico();
	}

	// === MANUAL REGION END ===

}
