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
import es.mityc.facturae32.IndividualType;
import es.mityc.facturae32.TaxIdentificationType;

// === IMPORT REGION END ===

@Embeddable
public class EmisorReceptor {
	// Código de los atributos

	public String nombreCompleto;

	public String identificacionFiscal;

	public EmisorReceptor() {
		init();
	}

	public void init() {

		if (nombreCompleto == null)
			nombreCompleto = new String();

	}

	// === MANUAL REGION START ===

	public static EmisorReceptor getDataFromEmisorReceptor(IndividualType individualType, TaxIdentificationType taxIdentificationType) {

		if (individualType != null) {
			EmisorReceptor emisorReceptor = new EmisorReceptor();
			emisorReceptor.nombreCompleto = individualType.getName() + " " + individualType.getFirstSurname() + " " + (individualType.getSecondSurname() != null ? individualType.getSecondSurname() : "");
			emisorReceptor.identificacionFiscal = taxIdentificationType.getTaxIdentificationNumber();
			return emisorReceptor;
		}
		return null;
	}

	// === MANUAL REGION END ===

}
