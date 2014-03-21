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
public class MetadatoTipoPatron extends Metadato {
	// Código de los atributos

	public void init() {
		super.init();

		postInit();
	}

	// === MANUAL REGION START ===
	@Override
	public boolean esValido() {
		if(!super.esValido()) {
			return false;
		}
		List<String> patrones = EsquemaMetadato.get(nombre).patron;
		if ((patrones == null) || (patrones.isEmpty())) {
			return true;
		}
		for(String patron : patrones) {
			if (valor.matches(patron)) {
				return true;
			}
		}
		return false;
	}

	// === MANUAL REGION END ===

}
