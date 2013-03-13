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
public class TipoCertificado extends FapModel {
	// Código de los atributos

	public String nombre;

	public String descripcion;

	@ValueFromTable("tiposDocumentos")
	public String tipoDocumento;

	public String nombrePlantilla;

	public Boolean necesitaFirma;

	public Integer validez;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaInicio"), @Column(name = "fechaInicioTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaInicio;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaFin"), @Column(name = "fechaFinTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaFin;

	public TipoCertificado() {
		init();
	}

	public void init() {

		if (necesitaFirma == null)
			necesitaFirma = false;

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
