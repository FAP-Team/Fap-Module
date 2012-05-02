
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
public class Exclusion extends Model {
	// Código de los atributos
	
	
	
	public String motivoExclusion;
	
	
	
	@ElementCollection
	public List<String> codigosExclusionString;
	
	
	
	@Transient
	public List<TipoCodigoExclusion> codigosExclusion;
	
	
	public Exclusion (){
		init();
	}
	

	public void init(){
		
		
							if (codigosExclusionString == null)
								codigosExclusionString = new ArrayList<String>();
							
						if (codigosExclusion == null)
							codigosExclusion = new ArrayList<TipoCodigoExclusion>();
						
	}
		
	

// === MANUAL REGION START ===
	
	public List<TipoCodigoExclusion> getCodigosExclusion () {
		List<TipoCodigoExclusion> codigos = new ArrayList<TipoCodigoExclusion>();
		for (String codeString : codigosExclusionString) {
			TipoCodigoExclusion code = TipoCodigoExclusion.find("select tipoCodigoExclusion from TipoCodigoExclusion tipoCodigoExclusion where tipoCodigoExclusion.codigo=?", codeString).first();
			if (code != null) {
				codigos.add(code);
			}
		}
		return codigos;
	}
			
// === MANUAL REGION END ===
	
	
	}
		