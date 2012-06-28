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
public class CEconomicosSolicitante extends FapModel {
	// Código de los atributos

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "ceconomicossolicitante_ceconomicosautomaticos")
	public List<CEconomico> ceconomicosAutomaticos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public CEconomicoSolicitanteManual otros;

	public CEconomicosSolicitante() {
		init();
	}

	public void init() {

		if (ceconomicosAutomaticos == null)
			ceconomicosAutomaticos = new ArrayList<CEconomico>();

		if (otros == null)
			otros = new CEconomicoSolicitanteManual();
		else
			otros.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
