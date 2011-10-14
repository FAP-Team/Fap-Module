
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
public class Participacion extends Model {
	// Código de los atributos
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public Agente agente;
	
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public SolicitudGenerica solicitud;
	
	
	@ValueFromTable("TiposParticipacion")
	public String tipo;
	
	

	public void init(){
		
		
	}
		
	
	

// === MANUAL REGION START ===
// === MANUAL REGION END ===
	
	
	}
		