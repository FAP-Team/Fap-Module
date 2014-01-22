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
public class SolicitudPortafirmaFAP extends FapModel {
	// Código de los atributos

	public String codSolicitudPf;

	public String estadoPf;

	public String tituloPf;

	public String descripcionPf;

	public String solicitantePf;

	public String destinatarioPf;

	public String emailNotificacionPf;

	public String comentarioPf;

	public String urlCorreoRedireccionPf;

	public String flujoAnteriorPf;

	public String usuarioLDAP;

	public String passwordLDAP;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
