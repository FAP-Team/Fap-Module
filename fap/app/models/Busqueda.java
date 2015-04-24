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
public class Busqueda extends Singleton {
	// Código de los atributos

	public String solicitud;

	public String interesado;

	@ElementCollection
	@ValueFromTable("estadosSolicitud")
	@FapEnum("enumerado.fap.gen.EstadosSolicitudEnum")
	public Set<String> estadoSolicitud;

	public Boolean mostrarTabla;

	public Busqueda() {
		init();
	}

	public void init() {
		super.init();

		if (estadoSolicitud == null)
			estadoSolicitud = new HashSet<String>();

		if (mostrarTabla == null)
			mostrarTabla = false;

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
