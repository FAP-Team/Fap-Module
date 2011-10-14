
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
public class Aportaciones extends Model {
	// Código de los atributos
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public Aportacion actual;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="aportaciones_registradas")
	public List<Aportacion> registradas;
	
	
	public Aportaciones (){
		init();
	}
	

	public void init(){
		
		
						if (actual == null)
							actual = new Aportacion();
						else
							actual.init();
					
						if (registradas == null)
							registradas = new ArrayList<Aportacion>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		