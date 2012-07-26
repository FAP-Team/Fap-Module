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
import enumerado.fap.gen.EstadoNotificacionEnum;
import properties.FapProperties;

// === IMPORT REGION END ===

@Entity
public class Notificacion extends FapModel {
	// Código de los atributos

	public String uri;

	public String uriProcedimiento;

	public String descripcion;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "notificacion_interesados")
	public List<Interesado> interesados;

	@ValueFromTable("estadoNotificacion")
	public String estado;

	@Transient
	public boolean activa;

	public String asunto;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaPuestaADisposicion"), @Column(name = "fechaPuestaADisposicionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaPuestaADisposicion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaAcceso"), @Column(name = "fechaAccesoTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaAcceso;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "notificacion_documentosanotificar")
	public List<DocumentoNotificacion> documentosANotificar;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "notificacion_documentosanexos")
	public List<DocumentoNotificacion> documentosAnexos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "notificacion_documentosrespuesta")
	public List<DocumentoNotificacion> documentosRespuesta;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "notificacion_documentosauditoria")
	public List<Documento> documentosAuditoria;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documentoPuestaADisposicion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documentoAnulacion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documentoRespondida;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documentoAcuseRecibo;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	public Integer plazoAcceso;

	public Integer frecuenciaRecordatorioAcceso;

	public Integer plazoRespuesta;

	public Integer frecuenciaRecordatorioRespuesta;

	public String idExpedienteAed;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Agente agente;

	public Boolean preparadaAnulacion;

	public Boolean preparadaRespondida;

	public Notificacion() {
		init();
	}

	public void init() {

		if (interesados == null)
			interesados = new ArrayList<Interesado>();

		if (documentosANotificar == null)
			documentosANotificar = new ArrayList<DocumentoNotificacion>();

		if (documentosAnexos == null)
			documentosAnexos = new ArrayList<DocumentoNotificacion>();

		if (documentosRespuesta == null)
			documentosRespuesta = new ArrayList<DocumentoNotificacion>();

		if (documentosAuditoria == null)
			documentosAuditoria = new ArrayList<Documento>();

		if (documentoPuestaADisposicion == null)
			documentoPuestaADisposicion = new Documento();
		else
			documentoPuestaADisposicion.init();

		if (documentoAnulacion == null)
			documentoAnulacion = new Documento();
		else
			documentoAnulacion.init();

		if (documentoRespondida == null)
			documentoRespondida = new Documento();
		else
			documentoRespondida.init();

		if (documentoAcuseRecibo == null)
			documentoAcuseRecibo = new Documento();
		else
			documentoAcuseRecibo.init();

		if (registro == null)
			registro = new Registro();
		else
			registro.init();
		preparadaAnulacion = false;
		preparadaRespondida = false;

		postInit();
	}

	// === MANUAL REGION START ===

	public Notificacion(List<DocumentoNotificacion> documentosANotificar, List<Interesado> interesados, String idExpedienteAed) {
		init();
		this.estado = EstadoNotificacionEnum.creada.name();
		this.plazoAcceso = FapProperties.getInt("fap.notificacion.plazoacceso");
		this.plazoRespuesta = FapProperties.getInt("fap.notificacion.plazorespuesta");
		this.frecuenciaRecordatorioAcceso = FapProperties.getInt("fap.notificacion.frecuenciarecordatorioacceso");
		this.frecuenciaRecordatorioRespuesta = FapProperties.getInt("fap.notificacion.frecuenciarecordatoriorespuesta");
		this.fechaPuestaADisposicion = new DateTime();
		this.documentosANotificar.addAll(documentosANotificar);
		this.interesados.addAll(interesados);
		this.idExpedienteAed = idExpedienteAed;
	}

	public boolean getActiva() {
		if ((this.estado != null) && (!this.estado.equals(EstadoNotificacionEnum.puestaadisposicion.name()))) {
			return true;
		}
		return false;
	}

	public List<Firmante> getFirmantes() {
		List<Firmante> listFirmantes = new ArrayList<Firmante>();

		for (Interesado interesado : interesados) {
			Firmante firmante = new Firmante(interesado);
			listFirmantes.add(firmante);
		}
		return listFirmantes;
	}

	public void actualizar(Notificacion notificacion) {
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Job");
		log.info("Viendo si hay que cambiar el estado de una notificacion. Antes: " + this.estado + " nuevo valor: " + notificacion.estado);
		log.info("Viendo si hay que cambiar la fecha de Acceso de una notificacion. Antes: " + this.fechaAcceso + " nuevo valor: " + notificacion.fechaAcceso);
		this.estado = notificacion.estado;
		this.fechaAcceso = notificacion.fechaAcceso;
	}

	// === MANUAL REGION END ===

}
