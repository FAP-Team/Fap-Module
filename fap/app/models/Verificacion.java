
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
public class Verificacion extends Model {
	// Código de los atributos
	
	
	public String uriVerificacion;
	
	
	
	public String uriProcedimiento;
	
	
	
	public String uriTramite;
	
	
	
	public String expediente;
	
	
	
	public String estado;
	
	
	@OneToMany(cascade=CascadeType.ALL ,  fetch=FetchType.LAZY)
	@JoinTable(name="verificacion_documentos")
	@Transient
	public List<VerificacionDocumento> documentos;
	
	
	
	public String uriExclusion;
	
	
	
	public String motivoExclusion;
	
	
	@OneToMany(cascade=CascadeType.ALL ,  fetch=FetchType.LAZY)
	@JoinTable(name="verificacion_codigosexclusion")
	@Transient
	public List<Exclusion> codigosExclusion;
	
	
	@OneToOne(cascade=CascadeType.ALL ,  fetch=FetchType.LAZY)
	public Requerimiento requerimientoProceso;
	
	
	@OneToMany(cascade=CascadeType.ALL ,  fetch=FetchType.LAZY)
	@JoinTable(name="verificacion_requerimientos")
	public List<Requerimiento> requerimientos;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaCreacion"),@Column(name="fechaCreacionTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaUltimaActualizacion"),@Column(name="fechaUltimaActualizacionTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaUltimaActualizacion;
	
	
	public Verificacion (){
		init();
	}
	

	public void init(){
		
		
						if (documentos == null)
							documentos = new ArrayList<VerificacionDocumento>();
						
						if (codigosExclusion == null)
							codigosExclusion = new ArrayList<Exclusion>();
						
						if (requerimientoProceso == null)
							requerimientoProceso = new Requerimiento();
						else
							requerimientoProceso.init();
					
						if (requerimientos == null)
							requerimientos = new ArrayList<Requerimiento>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		