
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
			
// === IMPORT REGION END ===
	


@Entity
public class ExpedienteAed extends Model {
	// Código de los atributos
	
	
	public String idAed;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===

	/**
	 * Asigna un ID de expediente único
	 */
	public void asignarIdAed(){
		Long id = SemillaExpediente.obtenerId();
		java.text.NumberFormat formatter = new java.text.DecimalFormat("0000");
		String prefijo = FapProperties.get("fap.aed.expediente.prefijo");
		idAed = prefijo + formatter.format(id);
		save();
	}
	
// === MANUAL REGION END ===
	
	
	}
		