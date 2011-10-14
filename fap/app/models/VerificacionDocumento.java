
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

// === IMPORT REGION START ===
			
// === IMPORT REGION END ===
	

@Auditable
@Entity
public class VerificacionDocumento extends Model {
	// Código de los atributos
	
	
	public String uriDocumentoVerificacion;
	
	
	
	public String uriDocumento;
	
	
	
	public String uriTipoDocumento;
	
	
	
	public String etiquetaTipoDocumento;
	
	
	
	public String descripcion;
	
	
	
	public String estadoDocumentoVerificacion;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaPresentacion"),@Column(name="fechaPresentacionTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaPresentacion;
	
	
	
	public String identificadorMultiple;
	
	
	
	public Integer version;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String motivoRequerimiento;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="verificaciondocumento_codigosrequerimiento")
	public List<CodigoRequerimiento> codigosRequerimiento;
	
	
	
	public Boolean existe;
	
	
	public VerificacionDocumento (){
		init();
	}
	

	public void init(){
		
		
						if (codigosRequerimiento == null)
							codigosRequerimiento = new ArrayList<CodigoRequerimiento>();
						
	}
		
	
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		