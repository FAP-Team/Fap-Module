
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

import properties.FapProperties;

// === IMPORT REGION END ===
	


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
	
	
	
	public Double totalCriterios;
	
	
	
	public Double inversionTotalAprobada;
	
	
	
	public Double subvencionTotalConcedida;
	
	
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
		if(tipo.tiposDocumentos.size() > 0){
			JPAQuery jpaQuery = Documento.find("select documento" +
							" from Solicitud solicitud" +
							" join solicitud.documentacion.documentos documento" +
							" where solicitud.id=:id and documento.tipo in (:tipos)");
			jpaQuery.query.setParameter("id", solicitud.id);
			jpaQuery.query.setParameter("tipos", tipo.tiposDocumentos);
			List<Documento> documentosAccesibles = jpaQuery.fetch();
			return documentosAccesibles;
		}else{
			return null;
		}
	}
	
	/**
	 * Devuelve el criterio que tiene cierta jerarquía
	 * @param jerarquia
	 * @return
	 */
	public Criterio getCriterio(String jerarquia){
		return Criterio.find("select criterio from Evaluacion evaluacion " +
				"join evaluacion.criterios criterio " +
				"where criterio.tipo.jerarquia=? " +
				"and evaluacion.id=?", jerarquia, id).first();
	}
	
	public CEconomico getCEconomico(String jerarquia){
		return CEconomico.find("select ceconomico from Evaluacion evaluacion " +
				"join evaluacion.ceconomicos ceconomico " +
				"where ceconomico.tipo.jerarquia=? " +
				"and evaluacion.id=?", jerarquia, id).first();		
	}
	
	public Double getSubvencionTotalConcedida(){
		Double totalConcedida = 0D;
		if(inversionTotalAprobada != null){
			String porcentajeAyudaString = FapProperties.get("fap.app.baremacion.porcentajeAyuda");
			Double porcentajeAyuda = Double.valueOf(porcentajeAyudaString);
			totalConcedida = ((this.inversionTotalAprobada*porcentajeAyuda)/100D);
		}
		return totalConcedida;
	}
	
	// === MANUAL REGION END ===
	
	
	}
		