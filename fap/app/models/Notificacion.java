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
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionService;
import properties.FapProperties;
import utils.NotificacionUtils;

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

	@Transient
	public String todosInteresados;

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

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaLimite"), @Column(name = "fechaLimiteTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaLimite;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaFinPlazo"), @Column(name = "fechaFinPlazoTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaFinPlazo;

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
	public Documento documentoNoAcceso;

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

		if (documentoNoAcceso == null)
			documentoNoAcceso = new Documento();
		else
			documentoNoAcceso.init();

		if (registro == null)
			registro = new Registro();
		else
			registro.init();

		if (preparadaAnulacion == null)
			preparadaAnulacion = false;

		if (preparadaRespondida == null)
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

	public void actualizarDocumentacion(Notificacion notificacion) {
		//Comprobación de que hay nueva documentación
		//LO QUE SE ALMACENAN EN BBDD SON LAS URIS
		//Se suben los expedientes al AED

		//List donde se almacenan los nuevos documentos a subir al AED
		List<DocumentoNotificacion> documentosNuevos = new ArrayList<DocumentoNotificacion>();

		//DocNotificacion (Requerimiento y/o más docs)
		for (DocumentoNotificacion doc : notificacion.documentosANotificar) {
			boolean encontrado = false;
			for (DocumentoNotificacion docDB : this.documentosANotificar) {
				if (doc.uri.equalsIgnoreCase(docDB.uri)) {
					encontrado = true;
					break;
				}
			}

			if (!encontrado) {
				this.documentosANotificar.add(doc);
				doc.save();
				this.save();
				// Preparar para almacenar en AED
				documentosNuevos.add(doc);
			}
		}

		// Sincronizar los documentos asociados
		for (DocumentoNotificacion doc : notificacion.documentosAnexos) {
			boolean encontrado = false;
			for (DocumentoNotificacion docDB : this.documentosAnexos) {
				if (doc.uri.equalsIgnoreCase(docDB.uri)) {
					encontrado = true;
					break;
				}
			}

			if (!encontrado) {
				this.documentosAnexos.add(doc);
				doc.save();
				this.save();
				// Preparar para almacenar en AED
				documentosNuevos.add(doc);
			}
		}

		//Doc Acuse de recibo -> Negativo o Positivo
		String uriAcuseDeRecibo = NotificacionUtils.obtenerUriDocumentos(this, DocumentoNotificacionEnumType.ACUSE_RECIBO);
		if ((uriAcuseDeRecibo != "") && (!uriAcuseDeRecibo.equals(this.documentoAcuseRecibo.uri))) {
			System.out.println("Nuevo fichero de AcuseRecibo para " + this.idExpedienteAed);
			NotificacionUtils.subirDocumentoNotificacionExpediente(uriAcuseDeRecibo, this);
			this.documentoAcuseRecibo.uri = uriAcuseDeRecibo;
		}

		//Doc anulacion
		String uriAnulacion = NotificacionUtils.obtenerUriDocumentos(this, DocumentoNotificacionEnumType.ANULACION);
		if ((uriAnulacion != "") && (!uriAnulacion.equals(this.documentoAnulacion.uri))) {
			System.out.println("Nuevo fichero de Anulacion para " + this.idExpedienteAed);
			NotificacionUtils.subirDocumentoNotificacionExpediente(uriAnulacion, this);
			this.documentoAnulacion.uri = uriAnulacion;
		}

		//DocPuestaADisposicion
		String uriPuestaADisposicion = NotificacionUtils.obtenerUriDocumentos(this, DocumentoNotificacionEnumType.PUESTA_A_DISPOSICION);
		if ((uriPuestaADisposicion != "") && (!uriPuestaADisposicion.equals(this.documentoPuestaADisposicion.uri))) {
			System.out.println("Nuevo fichero de PuestaADisposicion para " + this.idExpedienteAed);
			NotificacionUtils.subirDocumentoNotificacionExpediente(uriPuestaADisposicion, this);
			this.documentoPuestaADisposicion.uri = uriPuestaADisposicion;
		}

		//DocRespondida
		String uriRespondida = NotificacionUtils.obtenerUriDocumentos(this, DocumentoNotificacionEnumType.MARCADA_RESPONDIDA);
		
		//Código temporal: ya hay notificaciones creadas, que no tienen este doc inicialiado (nuevo doc)
		if (this.documentoNoAcceso == null){
			this.documentoNoAcceso = new Documento();
		}
		if ((uriRespondida != "") && (!uriRespondida.equals(this.documentoRespondida.uri))) {
			System.out.println("Nuevo fichero de Respondida para " + this.idExpedienteAed);
			NotificacionUtils.subirDocumentoNotificacionExpediente(uriRespondida, this);
			this.documentoRespondida.uri = uriRespondida;
		}

		//DocNoAcceso
		String uriNoAcceso = NotificacionUtils.obtenerUriDocumentos(this, DocumentoNotificacionEnumType.NO_ACCESO);
		if ((uriNoAcceso != "")  && (!uriNoAcceso.equals(this.documentoNoAcceso.uri))){
			System.out.println("Nuevo fichero de NoAcceso para " + this.idExpedienteAed);
			NotificacionUtils.subirDocumentoNotificacionExpediente(uriNoAcceso, this);
			this.documentoNoAcceso.uri = uriNoAcceso;
		}

		//Subida de los nuevos documentos de tipo DocumentoNotificacion (lista docs no es vacía)
		if ((documentosNuevos != null) && (!documentosNuevos.isEmpty())) {
			System.out.println("Nuevos Multiples Ficheros para " + this.idExpedienteAed);
			NotificacionUtils.subirDocumentosNotificacionExpediente(documentosNuevos, this);
		}
	}

	public void actualizar(Notificacion notificacion) {
		//org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Job");
		if ((this.estado != notificacion.estado) || (this.fechaFinPlazo != notificacion.fechaFinPlazo)) {
			//log.info("Viendo si hay que cambiar el estado de una notificacion. Antes: " + this.estado + " nuevo valor: " + notificacion.estado);
			//log.info("Viendo si hay que cambiar la fecha de Acceso de una notificacion. Antes: " + this.fechaAcceso + " nuevo valor: " + notificacion.fechaAcceso);
			this.estado = notificacion.estado;
			//this.fechaAcceso = notificacion.fechaAcceso;
			this.fechaFinPlazo = notificacion.fechaFinPlazo;
			this.fechaLimite = notificacion.fechaLimite;
			System.out.println("Actualizando fechas y estado de Notificacion: " + this.id + " para el expediente " + this.idExpedienteAed);
		}

	}

	public String getTodosInteresados() {
		String todos = "";
		for (Interesado interesado : this.interesados) {
			todos += interesado.persona.numeroId + ", ";
		}
		if (!todos.isEmpty())
			todos = todos.substring(0, todos.length() - 2);
		return todos;
	}

	// === MANUAL REGION END ===

}
