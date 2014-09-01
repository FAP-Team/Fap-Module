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
import org.hibernate.annotations.DiscriminatorOptions;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import utils.FechaUtils;

@DiscriminatorOptions(force = true)
// === IMPORT REGION END ===
@Entity
public class Convocatoria extends Singleton {
	// Código de los atributos

	@ValueFromTable("estadoConvocatoria")
	public String estado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ExpedienteAed expedienteAed;

	public Convocatoria() {
		init();
	}

	public void init() {
		super.init();
		if (estado == null)
			estado = "presentacion";

		if (expedienteAed == null)
			expedienteAed = new ExpedienteAed();
		else
			expedienteAed.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public static final String ANUAL = "anual";
	public static final String PREFIJO_CONVOCATORIA_ANUAL = "A";

	public static String getModalidadConvocatoria() {
		return FapProperties.get("fap.aed.expediente.modalidad");
	}

	public static String getIdentificadorConvocatoria(PropertyPlaceholder propertyPlaceholder) {
		String prefijo = propertyPlaceholder.get("fap." + propertyPlaceholder.get("fap.defaultAED") + ".convocatoria");
		if (PREFIJO_CONVOCATORIA_ANUAL.equals(prefijo)) {
			return PREFIJO_CONVOCATORIA_ANUAL + FechaUtils.getAnyoActual();
		} else {
			return prefijo;
		}
	}

	public static boolean esAnual() {
		return ANUAL.equals(getModalidadConvocatoria());
	}

	// === MANUAL REGION END ===

}
