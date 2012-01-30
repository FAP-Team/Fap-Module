
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
public class Quartz extends Singleton {
	// Código de los atributos
	
	
	
	
	public Boolean execute;
	
	
	
	
	
	public Boolean mostrarTodasSolicitudes;
	
	
	
	
	
	public Boolean mostrarSolicitudesRequeridas;
	
	
	
	
	
	public Boolean ejecutarCambioDeFecha;
	
	
	
	
	
	public Boolean cambiarEstadoPlazoVencido;
	
	
	
	
	
	public Boolean sendMail;
	
	
	
	
	
	public String texto;
	
	

	public void init(){
		super.init();
		
	}
		
	

// === MANUAL REGION START ===

// === MANUAL REGION END ===
	
	
	}
		