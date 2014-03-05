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
public class SolicitudFirmaPortafirma extends FapModel {
	// Código de los atributos

	public String idSolicitante;

	public String idDestinatario;

	public String tema;

	public String materia;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "plazoMaximo"), @Column(name = "plazoMaximoTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime plazoMaximo;

	public String solicitudEstadoComentario;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudfirmaportafirma_documentosfirma")
	public List<Documento> documentosFirma;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudfirmaportafirma_documentosconsulta")
	public List<Documento> documentosConsulta;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Agente agenteHaceSolicitud;

	public String tipoSolicitud;

	public String prioridad;

	public String emailNotificacion;

	public String urlRedireccion;

	public String urlNotificacion;

	public String flujoSolicitud;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documento;

	public String passwordSolicitante;

	public String uriFuncionarioSolicitante;

	public String uriFuncionarioDestinatario;

	public String uriSolicitud;

	public String procedimiento;

	public String expediente;

	public String tipoDocumento;

	public String mecanismoFirma;

	public String origen;

	public String solicitudEstado;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "solicitudEstadoFecha"), @Column(name = "solicitudEstadoFechaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime solicitudEstadoFecha;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "solicitudFechaInicio"), @Column(name = "solicitudFechaInicioTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime solicitudFechaInicio;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "solicitudFechaFin"), @Column(name = "solicitudFechaFinTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime solicitudFechaFin;

	public String firmaUriFuncionario;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "firmaFecha"), @Column(name = "firmaFechaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime firmaFecha;

	public Boolean firmaDelegada;

	public Boolean archivada;

	public String comentarioSolicitante;

	public SolicitudFirmaPortafirma() {
		init();
	}

	public void init() {

		if (documentosFirma == null)
			documentosFirma = new ArrayList<Documento>();

		if (documentosConsulta == null)
			documentosConsulta = new ArrayList<Documento>();

		if (agenteHaceSolicitud == null)
			agenteHaceSolicitud = new Agente();
		else
			agenteHaceSolicitud.init();

		if (documento == null)
			documento = new Documento();
		else
			documento.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
