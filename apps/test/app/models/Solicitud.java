
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
public class Solicitud extends SolicitudGenerica {
	// Código de los atributos
	
	@OneToOne(cascade=CascadeType.ALL ,  fetch=FetchType.LAZY)
	public DireccionTest direccionTest;
	
	
	@OneToOne(cascade=CascadeType.ALL ,  fetch=FetchType.LAZY)
	public ComboTest comboTest;
	
	
	@OneToOne(cascade=CascadeType.ALL ,  fetch=FetchType.LAZY)
	public SavePages savePages;
	
	
	public Solicitud (){
		init();
	}
	

	public void init(){
		super.init();
		
						if (direccionTest == null)
							direccionTest = new DireccionTest();
						else
							direccionTest.init();
					
						if (comboTest == null)
							comboTest = new ComboTest();
						else
							comboTest.init();
					
						if (savePages == null)
							savePages = new SavePages();
						else
							savePages.init();
					
	}
		
	
		
		public void savePagesPrepared () {
			}
			
	

// === MANUAL REGION START ===
		public Solicitud(Agente agente) {
			super.init();
			init();
			this.save();
			
			//Crea la participacion
			Participacion p = new Participacion();
			p.agente = agente;
			p.solicitud = this;
			p.tipo = "creador";
			p.save();
		}			
// === MANUAL REGION END ===
	
	
	}
		