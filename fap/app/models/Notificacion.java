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
import enumerado.fap.gen.EstadoNotificacionEnum;
// === IMPORT REGION END ===

@Entity
public class Notificacion extends FapModel {
	// Código de los atributos

	public String uri;

	public String descripcion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "notificacion_interesados")
	public List<Interesado> interesados;

	@ValueFromTable("estadoNotificacion")
	public String estado;

	@Transient
	public boolean activa;

	public Notificacion() {
		init();
	}

	public void init() {

		if (interesados == null)
			interesados = new ArrayList<Interesado>();

		postInit();
	}

	// === MANUAL REGION START ===

	public boolean getActiva() {
		if ((this.estado != null) && (!this.estado.equals(EstadoNotificacionEnum.puestaadisposicion.name()))) {
			return true;
		}
		return false;
	}

	// === MANUAL REGION END ===

}
