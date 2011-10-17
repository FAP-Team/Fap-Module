
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
	


@MappedSuperclass
public class Singleton extends Model {
	// Código de los atributos
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===

	public static <T extends Singleton> T get(Class<?> clazz){
		T first = null;
		try{
			JPAQuery q = (JPAQuery) clazz.getMethod("all", null).invoke(null, null);
			first = q.first();
			if (first == null){
				first = (T) clazz.getConstructor(null).newInstance(null);
				first.save();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		};
		return first;
	}
	
// === MANUAL REGION END ===
	
	
	}
		