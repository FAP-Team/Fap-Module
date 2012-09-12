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

/*
 Entidad Solicitud extends SolicitudGenerica{

 }
 */

@Entity
public class CuadroMando extends FapModel {
	// Código de los atributos

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "cuadromando_aplicacion")
	public List<Aplicacion> aplicacion;

	public CuadroMando() {
		init();
	}

	public void init() {

		if (aplicacion == null)
			aplicacion = new ArrayList<Aplicacion>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
