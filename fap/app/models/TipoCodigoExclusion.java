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
public class TipoCodigoExclusion extends FapModel {
	// Código de los atributos

	public String codigo;

	@Column(columnDefinition = "LONGTEXT")
	public String descripcion;

	public String descripcionCorta;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	/*Devuelve una lista con todos los códigos de exclusion */
	public static List<TipoCodigoExclusion> obtenerListaCodigosExclusion() {
		List<TipoCodigoExclusion> lst = new ArrayList<TipoCodigoExclusion>();
		lst = TipoCodigoExclusion.find("select tipos from TipoCodigoExclusion tipos order by tipos.codigo").fetch();
		return lst;
	}
	
	// === MANUAL REGION END ===

}
