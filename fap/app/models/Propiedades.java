
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
	

@Auditable
@Entity
public class Propiedades extends Model {
	// Código de los atributos
	
	
	public String descripcion;
	
	
	
	public String clave;
	
	
	@Transient
	public String valor;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===

	public String getValor() {
		return FapProperties.get(clave);
	}



	public void setValor(String valor) {}


// === MANUAL REGION END ===
	
	
	}
		
