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
public class DefinicionMetadatos extends FapModel {
	// Código de los atributos

	public String nombre;

	public String descripcion;

	public boolean autogenerado;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "definicionmetadatos_listametadatos")
	public List<Metadato> listaMetadatos;

	public DefinicionMetadatos() {
		init();
	}

	public void init() {

		if (listaMetadatos == null)
			listaMetadatos = new ArrayList<Metadato>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
