
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
public class Requerimiento extends Model {
	// Código de los atributos
	
	@Transient
	public String firma;
	
	
	@Transient
	public String urlDocRequerimiento;
	
	
	
	public String estado;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String motivo;
	
	
	
	public String firmante;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaRegistroSalida"),@Column(name="fechaRegistroSalidaTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistroSalida;
	
	
	
	public String numeroRegistroSalida;
	
	
	
	public String numeroGeneralRegistroSalida;
	
	
	
	public String oficinaRegistroSalida;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaAcuse"),@Column(name="fechaAcuseTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAcuse;
	
	
	
	public String uriDocRequerimiento;
	
	
	
	public Boolean docRequerimientoClasificado;
	
	
	
	public String uriDocJustificanteRequerimiento;
	
	
	
	public Boolean docJustificanteRequerimientoClasificado;
	
	
	
	public String uriRequerimiento;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaAcceso"),@Column(name="fechaAccesoTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAcceso;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaNotificacion"),@Column(name="fechaNotificacionTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaNotificacion;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaVencimiento"),@Column(name="fechaVencimientoTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaVencimiento;
	
	
	
	public Boolean accesoaSede;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaDisposicionSede"),@Column(name="fechaDisposicionSedeTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaDisposicionSede;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaAcuseSede"),@Column(name="fechaAcuseSedeTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAcuseSede;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaRechazoSede"),@Column(name="fechaRechazoSedeTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRechazoSede;
	
	
	
	public Boolean postal;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaEnvioPostal"),@Column(name="fechaEnvioPostalTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaEnvioPostal;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaAcusePostal"),@Column(name="fechaAcusePostalTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAcusePostal;
	
	

	public void init(){
		
		
	}
		
	
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		