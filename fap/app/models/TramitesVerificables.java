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
public class TramitesVerificables extends FapModel {
	// Código de los atributos

	public String uriTramite;

	@Transient
	public String nombre;

	public Boolean verificable;

	public TramitesVerificables() {
		init();
	}

	public void init() {

		if (verificable == null)
			verificable = true;

		postInit();
	}

	// === MANUAL REGION START ===

	public String getNombre() {
		String ret = Tramite.find("select t.nombre from Tramite t where t.uri=?", this.uriTramite).first();
		return ret;
	}

	// === MANUAL REGION END ===

}
