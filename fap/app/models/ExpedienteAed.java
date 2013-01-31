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
import properties.FapProperties;
import controllers.fap.IdentificadorExpedientesController;

// === IMPORT REGION END ===

@Auditable
@Entity
public class ExpedienteAed extends FapModel {
	// Código de los atributos

	public String idAed;

	@ValueFromTable("tipoCrearExpedienteAed")
	public String selectCrearExpedienteAed;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	/**
	 * Asigna un ID de expediente único
	 */
	public String asignarIdAed() {
		if (idAed == null) {
			try {
				if (selectCrearExpedienteAed == null)
					idAed = IdentificadorExpedientesController.invoke(IdentificadorExpedientesController.class, "getNuevoIdExpediente", "");
				else
					idAed = IdentificadorExpedientesController.invoke(IdentificadorExpedientesController.class, "getNuevoIdExpediente", selectCrearExpedienteAed);
			} catch (Throwable e) {
				play.Logger.error("No se pudo generar el identificador para el expediente. " + e);
			}
			this.save();
		}
		return idAed;
	}

	// === MANUAL REGION END ===

}
