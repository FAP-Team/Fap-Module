
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
public class ObligatoriedadDocumentos extends Singleton {
	// Código de los atributos
	
	@ElementCollection
	public List<String> imprescindibles;
	
	
	@ElementCollection
	public List<String> obligatorias;
	
	
	@ElementCollection
	public List<String> automaticas;
	
	
	@ElementCollection
	public List<String> manuales;
	
	

	public void init(){
		super.init();
		
	}
		
	

// === MANUAL REGION START ===
	
	public ObligatoriedadDocumentos (){
		if (imprescindibles == null)
			imprescindibles = new ArrayList<String>();
		if (obligatorias == null)
			obligatorias = new ArrayList<String>();
		if (automaticas == null)
			automaticas = new ArrayList<String>();
		if (manuales == null)
			manuales = new ArrayList<String>();
	}
	
	public void clear(){
		imprescindibles.clear();
		obligatorias.clear();
		automaticas.clear();
		manuales.clear();
	}
	
// === MANUAL REGION END ===
	
	
	}
		