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
public class VerificacionTramites extends Singleton {
	// Código de los atributos

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "verificaciontramites_tramites")
	public List<TramitesVerificables> tramites;

	public String uriTramitePorDefecto;

	public VerificacionTramites() {
		init();
	}

	public void init() {
		super.init();

		if (tramites == null)
			tramites = new ArrayList<TramitesVerificables>();

		postInit();
	}

	// === MANUAL REGION START ===

	public Long buscarTramiteByUri(String uriABuscar) {
		for (TramitesVerificables tv : this.tramites) {
			if (tv.uriTramite.equals(uriABuscar)) {
				return tv.id;
			}
		}
		return -1L;
	}

	// === MANUAL REGION END ===

}
