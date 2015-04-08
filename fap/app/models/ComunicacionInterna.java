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
public class ComunicacionInterna extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AsientoCIFap asiento;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnComunicacionInternaFap respuesta;

	@ValueFromTable("estadosComunicacionInterna")
	@FapEnum("enumerado.fap.gen.EstadosComunicacionInternaEnum")
	public String estado;

	public ComunicacionInterna() {
		init();
	}

	public void init() {

		if (asiento == null)
			asiento = new AsientoCIFap();
		else
			asiento.init();

		if (respuesta == null)
			respuesta = new ReturnComunicacionInternaFap();
		else
			respuesta.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
