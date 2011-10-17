
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
public class TipoCriterio extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String descripcion;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String instrucciones;
	
	
	@ValueFromTable("LstClaseCriterio")
	@Required
	public String claseCriterio;
	
	
	@Required
	public String jerarquia;
	
	
	
	public Integer valorPrecision;
	
	
	
	public Double valorMaximo;
	
	
	
	public Double valorMinimo;
	
	
	@ValueFromTable("LstTipoValorCriterio")
	@Required
	public String tipoValor;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="tipocriterio_listavalores")
	public List<CriterioListaValores> listaValores;
	
	
	
	public Boolean transparencia;
	
	
	
	public Boolean comentariosAdministracion;
	
	
	
	public Boolean comentariosSolicitante;
	
	
	public TipoCriterio (){
		init();
	}
	

	public void init(){
		
		
						if (listaValores == null)
							listaValores = new ArrayList<CriterioListaValores>();
						comentariosAdministracion = false;
comentariosSolicitante = false;

	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		