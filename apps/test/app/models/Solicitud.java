
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
	


@Entity
public class Solicitud extends SolicitudGenerica {
	// Código de los atributos
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public DireccionTest direccionTest;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public ComboTest comboTest;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public ValoresPorDefectoTest valoresPorDefectoTest;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public Fechas fechas;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public TestGrupo testGrupo;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="solicitud_tabladenombres")
	public List<TablaDeNombres> tablaDeNombres;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="solicitud_peta")
	public List<Peta> peta;
	
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public ComboTestRef comboError;
	
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="solicitud_comboerrormany")
	public List<ComboTestRef> comboErrorMany;
	
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public PaginasTab paginas;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="solicitud_popuppaginas")
	public List<TablaPopUpPaginas> popupPaginas;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
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
						
							if (valoresPorDefectoTest == null)
								valoresPorDefectoTest = new ValoresPorDefectoTest();
							else
								valoresPorDefectoTest.init();
						
							if (fechas == null)
								fechas = new Fechas();
							else
								fechas.init();
						
							if (testGrupo == null)
								testGrupo = new TestGrupo();
							else
								testGrupo.init();
						
						if (tablaDeNombres == null)
							tablaDeNombres = new ArrayList<TablaDeNombres>();
						
						if (peta == null)
							peta = new ArrayList<Peta>();
						
							if (comboError != null)
								comboError.init();	
						
						if (comboErrorMany == null)
							comboErrorMany = new ArrayList<ComboTestRef>();
						
							if (paginas != null)
								paginas.init();	
						
						if (popupPaginas == null)
							popupPaginas = new ArrayList<TablaPopUpPaginas>();
						
							if (savePages == null)
								savePages = new SavePages();
							else
								savePages.init();
						
	}
		
	public void savePagesPrepared () {
				if ((savePages.paginaSolicitante == null) || (!savePages.paginaSolicitante))
					Messages.error("La página Solicitante no fue guardada correctamente");
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
		