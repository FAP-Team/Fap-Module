
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
public class TablaPaginasTabTabTab extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	
	public Integer numero;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fecha"),@Column(name="fechaTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;
	
	
	@ValueFromTable("ComboTestList")
	public String list;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		