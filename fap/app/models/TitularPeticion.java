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
public class TitularPeticion extends FapModel {
	// Código de los atributos

	public String documentacion;

	@Transient
	public String nombreCompleto;

	public String nombre;

	public String apellido1;

	public String apellido2;

	@ValueFromTable("TipoDocumentacion")
	@FapEnum("enumerado.fap.gen.TipoDocumentacionEnum")
	public String tipoDocumentacion;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	/**
	 * Nombre completo: Unión de nombre, primerApellido y segundoApellido
	 * @return
	 */
	public String getNombreCompleto() {
		return utils.StringUtils.join(" ", nombre, apellido1, apellido2);
	}
	// === MANUAL REGION END ===

}
