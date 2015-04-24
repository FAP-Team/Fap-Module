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
public class Consulta extends FapModel {
	// Código de los atributos

	@Column(columnDefinition = "LONGTEXT")
	public String descripcion;

	@Column(columnDefinition = "LONGTEXT")
	public String consulta;

	@ValueFromTable("tipoConsulta")
	@FapEnum("enumerado.fap.gen.TipoConsultaEnum")
	public String tipo;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
