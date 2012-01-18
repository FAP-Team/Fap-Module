
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
public class TablaPopUpPaginas extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	
	public String apellido;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public Fechas fecha;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="tablapopuppaginas_tttpaginas")
	public List<TablaPaginasTabTabTab> tttpaginas;
	
	
	public TablaPopUpPaginas (){
		init();
	}
	

	public void init(){
		
		
							if (fecha == null)
								fecha = new Fechas();
							else
								fecha.init();
						
						if (tttpaginas == null)
							tttpaginas = new ArrayList<TablaPaginasTabTabTab>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		