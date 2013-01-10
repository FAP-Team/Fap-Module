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

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DireccionTest direccionTest;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ComboTest comboTest;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ValoresPorDefectoTest valoresPorDefectoTest;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Fechas fechas;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TestGrupo testGrupo;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitud_tabladenombres")
	public List<TablaDeNombres> tablaDeNombres;

	@ManyToOne(fetch = FetchType.LAZY)
	public ComboTestRef comboError;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitud_comboerrormany")
	public List<ComboTestRef> comboErrorMany;

	@ManyToOne(fetch = FetchType.LAZY)
	public PaginasTab paginas;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitud_popuppaginas")
	public List<TablaPopUpPaginas> popupPaginas;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Solicitante solicitantePersonaFisica;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Solicitante solicitantePersonaJuridica;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public PersonaFisica amigo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento doc;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitud_misconceptos")
	public List<MiConcepto> misConceptos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ConceptosMios conceptos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public BigPopUpPrueba bigPopUp;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ElementosBasicos elementos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public FirmaEnServidor firmaEnServidor;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitud_facturas")
	public List<FacturasFAP> facturas;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SavePages savePages;

	public Solicitud() {
		init();
	}

	public void init() {
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

		if (comboError != null)
			comboError.init();

		if (comboErrorMany == null)
			comboErrorMany = new ArrayList<ComboTestRef>();

		if (paginas != null)
			paginas.init();

		if (popupPaginas == null)
			popupPaginas = new ArrayList<TablaPopUpPaginas>();

		if (solicitantePersonaFisica == null)
			solicitantePersonaFisica = new Solicitante();
		else
			solicitantePersonaFisica.init();

		if (solicitantePersonaJuridica == null)
			solicitantePersonaJuridica = new Solicitante();
		else
			solicitantePersonaJuridica.init();

		if (amigo == null)
			amigo = new PersonaFisica();
		else
			amigo.init();

		if (doc == null)
			doc = new Documento();
		else
			doc.init();

		if (misConceptos == null)
			misConceptos = new ArrayList<MiConcepto>();

		if (conceptos == null)
			conceptos = new ConceptosMios();
		else
			conceptos.init();

		if (bigPopUp == null)
			bigPopUp = new BigPopUpPrueba();
		else
			bigPopUp.init();

		if (elementos == null)
			elementos = new ElementosBasicos();
		else
			elementos.init();

		if (firmaEnServidor == null)
			firmaEnServidor = new FirmaEnServidor();
		else
			firmaEnServidor.init();

		if (facturas == null)
			facturas = new ArrayList<FacturasFAP>();

		if (savePages == null)
			savePages = new SavePages();
		else
			savePages.init();

		postInit();
	}

	public void savePagesPrepared() {
		if ((savePages.paginaSolicitante == null) || (!savePages.paginaSolicitante))
			Messages.error("La página paginaSolicitante no fue guardada correctamente");

		if (TipoCEconomico.count() > 0) {
			if ((savePages.paginaPCEconomicos == null) || (!savePages.paginaPCEconomicos))
				Messages.error("La página Conceptos Económicos no fue guardada correctamente");
		}
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
