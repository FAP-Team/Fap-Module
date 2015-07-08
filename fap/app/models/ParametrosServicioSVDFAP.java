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
public class ParametrosServicioSVDFAP extends FapModel {
	// Código de los atributos

	public String nombreServicio;

	public String codigoProcedimiento;

	public String nombreProcedimiento;

	public String codigoCertificado;

	public Boolean consentimientoLey;

	@ValueFromTable("CaducidadCertificadosSVDFAP")
	@FapEnum("enumerado.fap.gen.CaducidadCertificadosSVDFAPEnum")
	public String caducidadCertificados;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
