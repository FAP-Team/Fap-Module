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
public class ServiciosWeb extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ServicioWebInfo servicioWebInfo;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "serviciosweb_peticion")
	public List<Peticion> peticion;

	public ServiciosWeb() {
		init();
	}

	public void init() {

		if (servicioWebInfo == null)
			servicioWebInfo = new ServicioWebInfo();
		else
			servicioWebInfo.init();

		if (peticion == null)
			peticion = new ArrayList<Peticion>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
