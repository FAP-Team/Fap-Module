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

@Embeddable
public class CCC {
	// Código de los atributos

	public String cccCodigoEntidad;

	public String cccCodigoOficina;

	public String cccDigitosControl;

	public String cccNumeroCuenta;

	public CCC() {
		init();
	}

	public void init() {

		if (cccCodigoEntidad == null)
			cccCodigoEntidad = new String();
		if (cccCodigoOficina == null)
			cccCodigoOficina = new String();
		if (cccDigitosControl == null)
			cccDigitosControl = new String();
		if (cccNumeroCuenta == null)
			cccNumeroCuenta = new String();

	}

	// === MANUAL REGION START ===

	public String getCCC() {
		if ((cccCodigoEntidad != null) && (!cccCodigoEntidad.isEmpty()) && (cccCodigoOficina != null) && (!cccCodigoOficina.isEmpty()) && (cccDigitosControl != null) && (!cccDigitosControl.isEmpty()) && (cccNumeroCuenta != null) && (!cccNumeroCuenta.isEmpty()))
			return cccCodigoEntidad + cccCodigoOficina + cccDigitosControl + cccNumeroCuenta;
		else
			return "";
	}

	// === MANUAL REGION END ===

}
