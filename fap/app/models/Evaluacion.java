
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
public class Evaluacion extends Model {
	// Código de los atributos
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public SolicitudGenerica solicitud;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="evaluacion_criterios")
	public List<Criterio> criterios;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="evaluacion_ceconomicos")
	public List<CEconomico> ceconomicos;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public TipoEvaluacion tipo;
	
	
	
	public String estado;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String comentariosAdministracion;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String comentariosSolicitante;
	
	
	public Evaluacion (){
		init();
	}
	

	public void init(){
		
		
						if (solicitud == null)
							solicitud = new SolicitudGenerica();
						else
							solicitud.init();
					
						if (criterios == null)
							criterios = new ArrayList<Criterio>();
						
						if (ceconomicos == null)
							ceconomicos = new ArrayList<CEconomico>();
						
						if (tipo == null)
							tipo = new TipoEvaluacion();
						else
							tipo.init();
					
	}
		
	

// === MANUAL REGION START ===
	public static Evaluacion init(TipoEvaluacion tipo){
		Evaluacion evaluacion = new Evaluacion();
		evaluacion.tipo = tipo;
		
		for(TipoCriterio tCriterio : tipo.criterios){
			Criterio criterio = new Criterio();
			criterio.tipo = tCriterio;
			evaluacion.criterios.add(criterio);
		}
		
		for(TipoCEconomico tCEconomico : tipo.ceconomicos){
			CEconomico cEconomico = new CEconomico();
			cEconomico.tipo = tCEconomico;
			evaluacion.ceconomicos.add(cEconomico);
		}
		
		return evaluacion;
	}
// === MANUAL REGION END ===
	
	
	}
		