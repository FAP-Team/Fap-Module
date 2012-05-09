
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
public class CodigoExclusion extends Model {
	// Código de los atributos
	
	
	
	public String codigo;
	
	
	
	@Transient
	public TipoCodigoExclusion tipoCodigo;
	
	
	public CodigoExclusion (){
		init();
	}
	

	public void init(){
		
		
							if (tipoCodigo == null)
								tipoCodigo = new TipoCodigoExclusion();
							else
								tipoCodigo.init();
						
	}
		
	

// === MANUAL REGION START ===
	public TipoCodigoExclusion getTipoCodigo () {
		if (this.codigo != null) {
			TipoCodigoExclusion tCode = TipoCodigoExclusion.find("select tCode from TipoCodigoExclusion tCode where tCode.codigo=?" , this.codigo).first();
			return tCode;
		}
		return tipoCodigo;
	}
// === MANUAL REGION END ===
	
	
	}
		