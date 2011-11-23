
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
public class Incidencia extends Model {
	// Código de los atributos
	
	@Email
	public String email;
	
	
	
	public String telefono;
	
	
	
	public String nombre;
	
	
	
	public String apellidos;
	
	
	
	public String asunto;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String texto;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===

// === MANUAL REGION END ===
	
	
	}
		