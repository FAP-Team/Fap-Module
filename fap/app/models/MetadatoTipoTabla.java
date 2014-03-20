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
public class MetadatoTipoTabla extends Metadato {
	// Código de los atributos

	public void init() {
		super.init();

		postInit();
	}

	// === MANUAL REGION START ===
	@Override
	public boolean esValido() {
		EsquemaMetadato esquema = EsquemaMetadato.get(nombre);
		if (esquema == null) {
			return false;
		}
		List<ValoresValidosMetadatos> validos = esquema.valores;
		if ((validos == null) || (validos.isEmpty())) {
			return false;
		}
		for(ValoresValidosMetadatos valido : validos) {
			if (valido.clave.equals(this.valor))
				return true;
		}
		
		return false;
	}

	// === MANUAL REGION END ===

}
