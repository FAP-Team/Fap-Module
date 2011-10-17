
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
public class CEconomico extends Model {
	// Código de los atributos
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public TipoCEconomico tipo;
	
	
	
	public Double valorSolicitado;
	
	
	
	public Double valorEstimado;
	
	
	
	public Double valorPropuesto;
	
	
	
	public Double valorConcedido;
	
	
	public CEconomico (){
		init();
	}
	

	public void init(){
		
		
						if (tipo == null)
							tipo = new TipoCEconomico();
						else
							tipo.init();
					
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		