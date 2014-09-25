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
import properties.FapPropertiesKeys;


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

	public static final String PREFIJO_CONVOCATORIA_ANUAL = "A";

	public static String getIdentificadorConvocatoria(PropertyPlaceholder propertyPlaceholder) {
		if (esAnual()) {
            return PREFIJO_CONVOCATORIA_ANUAL + FechaUtils.getAnyoActual();
        } else {
            String prefijo = propertyPlaceholder.get("fap." + propertyPlaceholder.get("fap.defaultAED") + ".convocatoria");
			return prefijo;
		}
	}

	public static boolean esAnual() {
		return PREFIJO_CONVOCATORIA_ANUAL.equals(FapProperties.get(FapPropertiesKeys.AED_CONVOCATORIA));
	}

	// === MANUAL REGION END ===

}
