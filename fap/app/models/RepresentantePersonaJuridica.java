
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

// === IMPORT REGION START ===
			
// === IMPORT REGION END ===
	

@Auditable
@Entity
public class RepresentantePersonaJuridica extends RepresentantePersonaFisica {
	// Código de los atributos
	
	@ValueFromTable("TiposRepresentantes")
	public String tipoRepresentacion;
	
	

	public void init(){
		super.init();
		
	}
		
	
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		