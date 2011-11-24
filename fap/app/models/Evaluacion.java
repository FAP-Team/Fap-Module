
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
		
		
							if (solicitud != null)
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
	public void init(TipoEvaluacion tipo) {
		this.tipo = tipo;
		for (TipoCriterio tCriterio : tipo.criterios) {
			Criterio criterio = new Criterio();
			criterio.tipo = tCriterio;
			this.criterios.add(criterio);
		}

		for (TipoCEconomico tCEconomico : tipo.ceconomicos) {
			CEconomico cEconomico = new CEconomico();
			cEconomico.tipo = tCEconomico;
			this.ceconomicos.add(cEconomico);
		}
	}
	
	/**
	 * Filtra de los documentos de la solicitud, los documentos
	 * cuyo tipo de documento está definido dentro de los 
	 * tipos de documentos accesibles por la definición 
	 * del tipo de la evaluación
	 * @return
	 */
	public List<Documento> getDocumentosAccesibles(){
		JPAQuery jpaQuery = Documento.find("select documento" +
						" from Solicitud solicitud" +
						" join solicitud.documentacion.documentos documento" +
						" where solicitud.id=:id and documento.tipo in (:tipos)");
		jpaQuery.query.setParameter("id", solicitud.id);
		jpaQuery.query.setParameter("tipos", tipo.tiposDocumentos);
		List<Documento> documentosAccesibles = jpaQuery.fetch();
		return documentosAccesibles;
	}
		
	// === MANUAL REGION END ===
	
	
	}
		