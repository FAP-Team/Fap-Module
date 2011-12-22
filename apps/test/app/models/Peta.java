
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
public class Peta extends Model {
	// Código de los atributos
	
	
	public String n;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fecha"),@Column(name="fechaTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;
	
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="peta_petador")
	public List<ComboTestRef> petador;
	
	
	
	public String d;
	
	
	
	public String otro;
	
	
	public Peta (){
		init();
	}
	

	public void init(){
		
		
						if (petador == null)
							petador = new ArrayList<ComboTestRef>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		