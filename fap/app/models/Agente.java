
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
@Inheritance(strategy=InheritanceType.JOINED)
public class Agente extends Model {
	// Código de los atributos
	
	
	
	public String username;
	
	
	
	
	public String password;
	
	
	
	@Email
	public String email;
	
	
	
	
	public String name;
	
	
	
	@ElementCollection
	@ValueFromTable("roles")
	public Set<String> roles;
	
	
	
	@ValueFromTable("roles")
	public String rolActivo;
	
	
	
	
	public String acceso;
	
	
	
	
	public Boolean funcionario;
	
	
	public Agente (){
		init();
	}
	

	public void init(){
		
		
			if (roles == null)
				roles = new HashSet<String>();
			
	}
		
	

// === MANUAL REGION START ===
	
	public void cambiarRolActivo(String rolActivo) {
		if (roles.contains(rolActivo)) {
			Logger.info("cambiando rol a " + rolActivo);
			this.rolActivo = rolActivo;
			this.save();
		} else {
			throw new RuntimeException("Intentando establecer un rolActivo que no tiene el usuario");
		}
	}
	
	@Override
	public String toString() {
		return "Agente [username=" + username + ", email=" + email + "]";
	}
	
	public boolean getFuncionario () {
		if (funcionario != null)
			return funcionario;
		return false;
	}

	/**
	 * Devuelve la lista de roles ordenados alfabéticamente
	 * @return
	 */
	public List<String> getSortRoles () {
		List<String> list = new ArrayList<String>(this.roles);	
		Collections.sort(list);
		return list;
	}
	
	// === MANUAL REGION END ===
	
	
	}
		