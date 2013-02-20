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
public class DatosAnotaciones extends FapModel {
	// Código de los atributos

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "datosanotaciones_anotaciones")
	public List<AnotacionFAP> anotaciones;

	@Transient
	public Boolean isResueltasTodasAnotaciones;

	public DatosAnotaciones() {
		init();
	}

	public void init() {

		if (anotaciones == null)
			anotaciones = new ArrayList<AnotacionFAP>();

		postInit();
	}

	// === MANUAL REGION START ===
	public Boolean getIsResueltasTodasAnotaciones() {
		if ((this.anotaciones == null) || (this.anotaciones.size() <= 0)) {
			return true;
		} else {
			for (AnotacionFAP anotacion : this.anotaciones) {
				if ((anotacion.checkResuelta == null) || (anotacion.checkResuelta == false))
					return false;
			}
		}
		return true;
	}
	// === MANUAL REGION END ===

}
