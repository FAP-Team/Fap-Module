
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
	


@Embeddable
public class Nip  {
	// Código de los atributos
	
	
	public String tipo;
	
	
	
	public String valor;
	
	
	public Nip (){
		init();
	}
	

	public void init(){
		
		
		if (tipo == null)
			tipo = new String();
	}
		
	

// === MANUAL REGION START ===
	@Override
	public boolean equals(Object otro){
		if (otro == null || !(otro instanceof Nip)){
			return false;
		}
		Nip nip = (Nip) otro;
		return nip.tipo.equals(tipo) && nip.valor.equals(valor);
	}
	
	@Override
	public String toString() {
		return "Nip [tipo=" + tipo + ", valor=" + valor + "]";
	}
	
	public boolean isNif(){
		return tipo.equals("nif");
	}
	
	public boolean isNie(){
		return tipo.equals("nie");
	}
	
	public String getPlatinoTipoDocumento(){
		if(isNif()){
			return "N";
		}else if(isNie()){
			return "E";
		}
		
		return null;
	}
// === MANUAL REGION END ===
	
	
	}
		