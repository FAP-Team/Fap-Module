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
public class RelacionWSConsultas extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ServiciosWebAplicacion serviciosWeb;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "relacionwsconsultas_consulta")
	public List<ConsultasWS> consulta;

	public RelacionWSConsultas() {
		init();
	}

	public void init() {

		if (serviciosWeb == null)
			serviciosWeb = new ServiciosWebAplicacion();
		else
			serviciosWeb.init();

		if (consulta == null)
			consulta = new ArrayList<ConsultasWS>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
