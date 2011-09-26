
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
@Inheritance(strategy=InheritanceType.JOINED)
public class PersonaFisica extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	
	public String primerApellido;
	
	
	
	public String segundoApellido;
	
	
	@CheckWith(NipCheck.class)
	@Embedded
	public Nip nip;
	
	
	public PersonaFisica (){
		init();
	}
	

	public void init(){
		
		
			if (nip == null)
				nip = new Nip();
			
	}
		
	
	

// === MANUAL REGION START ===
	/**
	 * Nombre completo: Unión de nombre, primerApellido y segundoApellido
	 * @return
	 */
	public String getNombreCompleto(){
		return utils.StringUtils.join(" ", nombre, primerApellido, segundoApellido);
	}
	
// === MANUAL REGION END ===
	
	
	}
		