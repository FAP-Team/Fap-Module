
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
	

@Auditable
@Entity
public class TipoCEconomico extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	
	public String descripcion;
	
	
	
	public String jerarquia;
	
	
	
	public Boolean comentariosAdministracion;
	
	
	
	public Boolean comentariosSolicitante;
	
	
	public TipoCEconomico (){
		init();
	}
	

	public void init(){
		
		comentariosAdministracion = false;
comentariosSolicitante = false;

	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		