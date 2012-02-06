
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
public class ComboTest extends Model {
	// Código de los atributos
	
	@ValueFromTable("provincias")
	public String provincias;
	
	
	@ValueFromTable("comunidadesAutonomas")
	public String comunidades;
	
	
	@ValueFromTable("paises")
	public String paises;
	
	
	@ValueFromTable("municipios")
	public String municipios;
	
	
	@ValueFromTable("ComboTestList")
	public String list;
	
	
	@ElementCollection
	@ValueFromTable("ComboTestList")
	public Set<String> listMultiple;
	
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public ComboTestRef ref;
	
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="combotest_refmultiple")
	public List<ComboTestRef> refMultiple;
	
	
	
	public String listOverwrite;
	
	
	
	public Long listOverwriteLong;
	
	
	@ElementCollection
	public Set<String> listMultipleOverwrite;
	
	
	@ElementCollection
	public List<Long> listMultipleOverwriteLong;
	
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public ComboTestRef refOverwrite;
	
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="combotest_refmultipleoverwrite")
	public List<ComboTestRef> refMultipleOverwrite;
	
	
	
	public Long wsjson;
	
	
	
	public Long wsxml;
	
	
	public ComboTest (){
		init();
	}
	

	public void init(){
		
		
			if (listMultiple == null)
				listMultiple = new HashSet<String>();
			
							if (ref != null)
								ref.init();	
						
						if (refMultiple == null)
							refMultiple = new ArrayList<ComboTestRef>();
						
							if (refOverwrite != null)
								refOverwrite.init();	
						
						if (refMultipleOverwrite == null)
							refMultipleOverwrite = new ArrayList<ComboTestRef>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		