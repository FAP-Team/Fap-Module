
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
	



public class Log extends Model {
	// Código de los atributos
	
	
	public String time;
	
	
	
	public String level;
	
	
	
	public String class_;
	
	
	
	public String user;
	
	
	
	public String message;
	
	
	
	public String trace;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		