
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
public class Firmante extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	
	public String idtipo;
	
	
	
	public String idvalor;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaFirma"),@Column(name="fechaFirmaTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaFirma;
	
	
	
	public String tipo;
	
	
	
	public String cardinalidad;
	
	

	public void init(){
		
		
	}
		
	
	

// === MANUAL REGION START ===
	public void setIdentificador(Nip nip){
		idtipo = nip.tipo;
		idvalor = nip.valor;
	}
	
	public void setIdentificador(String cif){
		idtipo = "cif";
		idvalor = cif;
	}

	
	public void setIdentificador(Persona p){
		if (p.isPersonaFisica())
			setIdentificador(p.fisica.nip);
		else if (p.isPersonaJuridica())
			setIdentificador(p.juridica.cif);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((idtipo == null) ? 0 : idtipo.hashCode());
		result = prime * result + ((idvalor == null) ? 0 : idvalor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Firmante other = (Firmante) obj;

		if (idtipo != null && other.idtipo != null && idvalor != null && other.idvalor != null) {
			//Por alguna razon los certificados no distingeun entre NIE Y NIF 
			//y se ponen los dos en el mismo campo como NIF
			
			String tipo = idtipo;
			if (tipo.equalsIgnoreCase("nie"))
				tipo = "nif";
			String otherTipo = other.idtipo;
			if(otherTipo.equalsIgnoreCase("nie")){
				otherTipo = "nif";
			}
			
			return tipo.equalsIgnoreCase(otherTipo) && idvalor.equalsIgnoreCase(other.idvalor);
		}
		return false;
	}

	@Override
	public String toString() {
		return "Firmante [nombre=" + nombre + ", idtipo=" + idtipo
				+ ", idvalor=" + idvalor + ", fechaFirma=" + fechaFirma
				+ ", tipo=" + tipo + "]";
	}
	
	
	
// === MANUAL REGION END ===
	
	
	}
		