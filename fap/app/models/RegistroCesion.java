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
public class RegistroCesion extends FapModel {
	// Código de los atributos

	public String tipoRegistro;

	public String nDocumento;

	public String estado;

	public String nombre;

	public String ident;

	public String cert;

	public String negat;

	public String datosPropios;

	public String referencia;

	public String regimen;

	public String cccPpal;

	public String numMedioTrabajadores;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaSolicitud"), @Column(name = "fechaSolicitudTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaSolicitud;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
