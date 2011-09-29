
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
public class FasesRegistro extends Model {
	// Código de los atributos
	
	
	public Boolean borrador;
	
	
	
	public Boolean firmada;
	
	
	
	public Boolean expedientePlatino;
	
	
	
	public Boolean registro;
	
	
	
	public Boolean expedienteAed;
	
	
	
	public Boolean clasificarAed;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===
public FasesRegistro() {
	reiniciar();
}

public void reiniciar(){
	borrador = false;
	firmada = false;
	expedientePlatino = false;
	registro = false;
	expedienteAed = false;
	clasificarAed = false;	
}


// === MANUAL REGION END ===
	
	
	}
		