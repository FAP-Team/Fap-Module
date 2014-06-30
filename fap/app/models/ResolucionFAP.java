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
import controllers.fap.ResolucionControllerFAP;

// === IMPORT REGION END ===

@Entity
public class ResolucionFAP extends FapModel {
	// Código de los atributos

	@ValueFromTable("resolucionesDefinidas")
	public String tipoDefinidoResolucion;

	@ValueFromTable("modalidadResolucion")
	public String modalidad;

	@ValueFromTable("tipoResolucion")
	public String tipo;

	@ValueFromTable("estadoTipoMultiple")
	public String tipoMultiple;

	public Boolean firmarJefeServicio;

	public Boolean firmarDirector;

	public Boolean permitirPortafirma;

	public Boolean permitirRegistrar;

	public Boolean permitirPublicar;

	@ValueFromTable("estadoResolucion")
	public String estado;

	@ValueFromTable("estadoResolucionNotificacion")
	public String estadoNotificacion;

	@ValueFromTable("estadoResolucionPublicacion")
	public String estadoPublicacion;

	@ValueFromTable("estadosDocBaremacion")
	public String estadoDocBaremacionResolucion;

	@ValueFromTable("estadosDocBaremacion")
	public String estadoInformeBaremacionConComentarios;

	@ValueFromTable("estadosDocBaremacion")
	public String estadoInformeBaremacionSinComentarios;

	@Column(length = 2500)
	public String descripcion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "resolucionfap_lineasresolucion")
	public List<LineaResolucionFAP> lineasResolucion;

	public Boolean conBaremacion;

	public Boolean oficialEvaluacionCompleto;

	public String tituloInterno;

	@Column(length = 1000)
	public String sintesis;

	@Column(length = 1000)
	public String observaciones;

	public Integer folio_inicio;

	public Integer folio_final;

	public Integer numero_folios;

	public Integer numero;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRegistroResolucion"), @Column(name = "fechaRegistroResolucionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistroResolucion;

	@ValueFromTable("areasResolucion")
	public String areasResolucion;

	@ValueFromTable("tiposResolucion")
	public String tiposResolucion;

	public String codigoResolucion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaFinAceptacion"), @Column(name = "fechaFinAceptacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaFinAceptacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaIncioPreparacion"), @Column(name = "fechaIncioPreparacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaIncioPreparacion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	public Boolean copiadoExpedientes;

	@OneToMany
	@Transient
	public List<Interesado> destinatarios;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ExpedienteAed expedienteAed;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudFirmaPortafirma solicitudFirmaPortafirma;

	public String idSolicitudFirma;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaTopeFirma"), @Column(name = "fechaTopeFirmaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaTopeFirma;

	public String prioridadFirma;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "resolucionfap_docconsultaportafirmasresolucion")
	public List<Documento> docConsultaPortafirmasResolucion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Agente hacePeticionPortafirma;

	public String jefeDeServicio;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudFirmaPortafirma solicitudFirmaPortafirmaOficioRemision;

	public String idSolicitudFirmaOficiosRemision;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaTopeFirmaOficiosRemision"), @Column(name = "fechaTopeFirmaOficiosRemisionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaTopeFirmaOficiosRemision;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Agente hacePeticionPortafirmaOficiosRemision;

	public String destinatarioOficioRemisionPortafirma;

	public Boolean resolucionReintegro;

	@Column(length = 1000)
	public String textoResolucionReintegro;

	public ResolucionFAP() {
		init();
	}

	public void init() {

		if (lineasResolucion == null)
			lineasResolucion = new ArrayList<LineaResolucionFAP>();

		if (conBaremacion == null)
			conBaremacion = false;

		if (oficialEvaluacionCompleto == null)
			oficialEvaluacionCompleto = false;

		if (registro == null)
			registro = new Registro();
		else
			registro.init();

		if (copiadoExpedientes == null)
			copiadoExpedientes = false;

		if (destinatarios == null)
			destinatarios = new ArrayList<Interesado>();

		if (expedienteAed == null)
			expedienteAed = new ExpedienteAed();
		else
			expedienteAed.init();

		if (solicitudFirmaPortafirma == null)
			solicitudFirmaPortafirma = new SolicitudFirmaPortafirma();
		else
			solicitudFirmaPortafirma.init();

		if (docConsultaPortafirmasResolucion == null)
			docConsultaPortafirmasResolucion = new ArrayList<Documento>();

		if (solicitudFirmaPortafirmaOficioRemision == null)
			solicitudFirmaPortafirmaOficioRemision = new SolicitudFirmaPortafirma();
		else
			solicitudFirmaPortafirmaOficioRemision.init();

		if (resolucionReintegro == null)
			resolucionReintegro = false;

		postInit();
	}

	// === MANUAL REGION START ===
	/**
	 * Calcula la lista de firmantes para la resolución
	 * @param agente
	 * @return
	 */
	public List<Firmante> calcularFirmantes() {
		Firmantes firmantes = new Firmantes();
		List<Agente> agentes = Agente.find("select agente from Agente agente join agente.roles rol where rol = 'jefeServicio'").fetch();
		for (int i = 0; i < agentes.size(); i++) {
			Firmante firmante = new Firmante(agentes.get(i));
			firmantes.todos.add(firmante);
		}

		return firmantes.todos;
	}

	public static List<Interesado> getInteresados(Long idResolucion) {
		List<Interesado> listaInteresados = new ArrayList<Interesado>();
		try {
			listaInteresados = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getInteresados", idResolucion);
		} catch (Throwable e) {
			play.Logger.error("Error obteniendo los interesados", e);
		}

		return listaInteresados;

	}

	public Agente getAgenteSolicitadoFirmaOficiosRemision() {
		Agente agente = null;
		agente = Agente.find("byUsername", this.idSolicitudFirmaOficiosRemision).first();
		if (agente == null) {
            agente = new Agente();
			agente.username = "";
            agente.cargo = "";
		}
		return agente;
	}

	// === MANUAL REGION END ===

}
