
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
public class TablaPaginasTab extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	
	public Integer numero;
	
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="tablapaginastab_combomul")
	public List<ComboTestRef> comboMul;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fecha"),@Column(name="fechaTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="tablapaginastab_ttpaginas")
	public List<TablaPaginasTabTab> ttpaginas;
	
	
	public TablaPaginasTab (){
		init();
	}
	

	public void init(){
		
		
						if (comboMul == null)
							comboMul = new ArrayList<ComboTestRef>();
						
						if (ttpaginas == null)
							ttpaginas = new ArrayList<TablaPaginasTabTab>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		