
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
public class TablaPaginasTabTab extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="tablapaginastabtab_combomul")
	public List<ComboTestRef> comboMul;
	
	
	@ValueFromTable("ComboTestList")
	public String list;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="tablapaginastabtab_tttpaginas")
	public List<TablaPaginasTabTabTab> tttpaginas;
	
	
	public TablaPaginasTabTab (){
		init();
	}
	

	public void init(){
		
		
						if (comboMul == null)
							comboMul = new ArrayList<ComboTestRef>();
						
						if (tttpaginas == null)
							tttpaginas = new ArrayList<TablaPaginasTabTabTab>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		