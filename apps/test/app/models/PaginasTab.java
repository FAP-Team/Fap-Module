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
public class PaginasTab extends Model {
	// Código de los atributos

	public String nombre;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "paginastab_tpaginas_nivel1")
	public List<TablaPaginas_nivel1> tpaginas_nivel1;

	public PaginasTab() {
		init();
	}

	public void init() {

		if (tpaginas_nivel1 == null)
			tpaginas_nivel1 = new ArrayList<TablaPaginas_nivel1>();

	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
