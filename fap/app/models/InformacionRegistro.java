
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
public class InformacionRegistro extends Model {
	// Código de los atributos
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaRegistro"),@Column(name="fechaRegistroTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;
	
	
	
	public String unidadOrganica;
	
	
	
	public String numeroRegistro;
	
	
	
	public String numeroRegistroGeneral;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===
	public void setDataFromJustificante(es.gobcan.platino.servicios.registro.JustificanteRegistro justificante){
		fechaRegistro = new DateTime(justificante.getDatosFirmados().getFechaRegistro().toGregorianCalendar());
		unidadOrganica = justificante.getDatosFirmados().getNúmeroRegistro().getOficina();
		numeroRegistro = justificante.getDatosFirmados().getNúmeroRegistro().getNumOficina().toString();
		numeroRegistroGeneral = justificante.getDatosFirmados().getNúmeroRegistro().getContent().get(0);
		save();
	}
// === MANUAL REGION END ===
	
	
	}
		