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
public class ServicioWebInfo extends FapModel {
	// Código de los atributos

	public String nombre;

	public String urlWS;

	public Boolean activo;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "serviciowebinfo_infoparams")
	public List<InfoParams> infoParams;

	public ServicioWebInfo() {
		init();
	}

	public void init() {

		if (infoParams == null)
			infoParams = new ArrayList<InfoParams>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
