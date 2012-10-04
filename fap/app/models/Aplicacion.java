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
public class Aplicacion extends FapModel {
	// Código de los atributos

	public String nombreApp;

	public String urlApp;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "aplicacion_serviciosweb")
	public List<ServiciosWeb> serviciosWeb;

	public Aplicacion() {
		init();
	}

	public void init() {

		if (serviciosWeb == null)
			serviciosWeb = new ArrayList<ServiciosWeb>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
