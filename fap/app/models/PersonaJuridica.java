
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
@Inheritance(strategy=InheritanceType.JOINED)
public class PersonaJuridica extends Model {
	// Código de los atributos
	
	
	public String entidad;
	
	
	@CheckWith(CifCheck.class)
	public String cif;
	
	
	@Email
	public String email;
	
	
	
	public String telefonoFijo;
	
	
	
	public String telefonoMovil;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaFirma"),@Column(name="fechaFirmaTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaFirma;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===

// === MANUAL REGION END ===
	
	
	}
		