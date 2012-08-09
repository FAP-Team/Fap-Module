package tramitacion;

import java.util.List;

import controllers.fap.VerificacionFapController;
import emails.Mails;

import properties.FapProperties;

import services.GestorDocumentalServiceException;
import services.RegistroServiceException;
import services.VerificarDocumentacionService;
import services.platino.PlatinoGestorDocumentalService;
import messages.Messages;
import models.Documento;
import models.DocumentoExterno;
import models.Registro;
import models.SolicitudGenerica;

public class TramiteAceptacionRenuncia extends TramiteBase {
	
	private final static String TIPO_TRAMITE = "solicitud";
	private final static String NOMBRE_TRAMITE = FapProperties.get("fap.aed.procedimientos.tramiteaceptacionrenuncia.nombre");
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.aceptacionrenuncia");
	private final static String BODY_REPORT_ACEPTACION = "reports/aceptacion.html";
	private final static String BODY_REPORT_RENUNCIA = "reports/renuncia.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAILACEPTACION = "aceptacionRealizada";
	private final static String MAILRENUNCIA = "renunciaRealizada";
	private final static String JUSTIFICANTE = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroAceptacionRenuncia");
	private PlatinoGestorDocumentalService platinoGestorDocumentalService;
	
	public TramiteAceptacionRenuncia(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	/**
	 * Retorna el registro del trámite aceptación
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.aceptarRenunciar.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteAceptacionRenuncia.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		if (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("acepta"))
			return TramiteAceptacionRenuncia.BODY_REPORT_ACEPTACION;
		else
			return TramiteAceptacionRenuncia.BODY_REPORT_RENUNCIA;
	}

	@Override
	public String getHeaderReport() {
		return TramiteAceptacionRenuncia.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteAceptacionRenuncia.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		if (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("acepta"))
			return TramiteAceptacionRenuncia.MAILACEPTACION;
		else
			return TramiteAceptacionRenuncia.MAILRENUNCIA;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteAceptacionRenuncia.JUSTIFICANTE;
	}

	@Override
	public String getDescripcionJustificante() {
		return TramiteAceptacionRenuncia.TIPO_TRAMITE;
	}

	@Override
	public String getTipoTramite() {
		return TramiteAceptacionRenuncia.TIPO_TRAMITE;
	}

	/**
	 * Retorna los documentos del trámite aceptación
	 */
	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.aceptarRenunciar.documentos;
	}

	/**
	 * Nombre del fichero del justificante para el trámite aceptación
	 */
	@Override
	public String getPrefijoJustificantePdf() {
		return FapProperties.get("fap.tramitacion.aceptacion.prefijojustificantepdf");
	}

	/**
	 * Salva el trámite de aceptación
	 */
	@Override
	public void guardar() {
		this.solicitud.aceptarRenunciar.save();
	}

	/**
	 * Crea el expediente en el AED
	 */
	@Override
	public void crearExpedienteAed() {
		if (!this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedienteAed){
			try {
				gestorDocumentalService.crearExpediente(this.solicitud);
				this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedienteAed = true;
				this.solicitud.aceptarRenunciar.registro.fasesRegistro.save();
			} catch (GestorDocumentalServiceException e) {
				play.Logger.debug("Error creando el expediente en el Gestor Documental", e.getMessage());
				Messages.error("Error creando el expediente en el Gestor Documental");
			}
		}
		else {
			play.Logger.debug("El expediente del aed para la solicitud %s ya está creado", this.solicitud.aceptarRenunciar.id);
		}

		//Cambiamos el estado de la solicitud
		if (this.solicitud.aceptarRenunciar.motivoRenuncia != null) {
			if (!this.solicitud.estado.equals("aceptadoRSLPROV")) {
				this.solicitud.estado = "aceptadoRSLPROV";
				this.solicitud.save();
				Mails.enviar(this.getMail(), this.solicitud);
			}
		}
		else {
			if (!this.solicitud.estado.equals("renunciadoRSLPROV")) {
				this.solicitud.estado = "renunciadoRSLPROV";
				this.solicitud.save();
				Mails.enviar(this.getMail(), this.solicitud);
			}
		}
		
	}

	/**
	 * Crea el expediente en el archivo electrónico de platino
	 */
	@Override
	public void crearExpedientePlatino() throws RegistroServiceException {
		
		if (!this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedientePlatino){
			try {
				platinoGestorDocumentalService.crearExpediente(this.solicitud.expedientePlatino);

				this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedientePlatino = true;
				this.solicitud.aceptarRenunciar.registro.fasesRegistro.save();
			} catch (Exception e) {
				Messages.error("Error creando expediente en el gestor documental de platino");
				throw new RegistroServiceException("Error creando expediente en el gestor documental de platino");
			}
		}
		else {
			play.Logger.debug("El expediente de platino para la solicitud %s ya está creado", solicitud.id);
		}
	}

	@Override
	public void anadirDocumentosSolicitud() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 *
	 */
	@Override
	public void validarReglasConMensajes() {
		this.validarDocumentacion();
	}

	/**
	 * Validar los documentos condicionados automaticamente
	 */
	public void validarDocumentacion() {
		
		if (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("aceptar")) {
			VerificarDocumentacionService verificar = new VerificarDocumentacionService("solicitud", this.getDocumentos(), this.getDocumentosExternos());
			List<String> condicionadosAutomaticosNoAportados;
			try {
				condicionadosAutomaticosNoAportados = VerificacionFapController.getDocumentosNoAportadosCondicionadosAutomaticos(NOMBRE_TRAMITE, solicitud.aceptarRenunciar.id);
				verificar.preparaPresentacionTramite(condicionadosAutomaticosNoAportados);
			} catch (Throwable e) {
				play.Logger.debug("Error validando la documentacion aportada", e.getMessage());
				Messages.error("Error validando la documentacion aportada");
			}
		}
		
	}

	/**
	 * No realiza cambios de estado
	 */
	@Override
	public
	final void cambiarEstadoSolicitud() {
		
	}

	@Override
	public List<DocumentoExterno> getDocumentosExternos() {
		// TODO Auto-generated method stub
		return solicitud.aceptarRenunciar.documentosExternos;
	}
	

}