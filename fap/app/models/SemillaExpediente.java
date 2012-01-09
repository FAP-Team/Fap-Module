
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
public class SemillaExpediente extends Singleton {
	// Código de los atributos
	
	
	public Long semilla;
	
	

	public void init(){
		super.init();
		
	}
		
	

// === MANUAL REGION START ===
	
	//Obtiene un ID de expediente, el ID debe ser único para todos los expedientes
	public static synchronized Long obtenerId(){
		SemillaExpediente semilla = SemillaExpediente.all().first();
		if(semilla == null){
			semilla = new SemillaExpediente();
			semilla.semilla = 1L; // Debe comenzar en 1 (Issue 54)
		}
		
		Long semillaActual = semilla.semilla;
		semilla.semilla++;
		semilla.save();
		return semillaActual;
	}
	
// === MANUAL REGION END ===
	
	
	}
		