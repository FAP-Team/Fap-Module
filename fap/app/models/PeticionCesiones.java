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
public class PeticionCesiones extends FapModel {
	// Código de los atributos

	@ValueFromTable("listaCesiones")
	public String tipo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento fichPeticion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento fichRespuesta;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaGen"), @Column(name = "fechaGenTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaGen;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaValidez"), @Column(name = "fechaValidezTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaValidez;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public RespuestaCesion respCesion;

	@ValueFromTable("estadosPeticion")
	public String estado;

	@ValueFromTable("seleccionExpedientesCesion")
	public String seleccion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCorte"), @Column(name = "fechaCorteTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCorte;

	public PeticionCesiones() {
		init();
	}

	public void init() {

		if (fichPeticion == null)
			fichPeticion = new Documento();
		else
			fichPeticion.init();

		if (fichRespuesta == null)
			fichRespuesta = new Documento();
		else
			fichRespuesta.init();

		if (respCesion == null)
			respCesion = new RespuestaCesion();
		else
			respCesion.init();

		postInit();
	}

	// === MANUAL REGION START ===
	//public String getRespondidaTxt() {
	//	if (respondida)
	//		return "Sí";
	//	return "No";
	//}
	// === MANUAL REGION END ===

}
