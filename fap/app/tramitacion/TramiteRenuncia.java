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
import models.Registro;
import models.SolicitudGenerica;

public class TramiteRenuncia extends TramiteBase {

	private final static String TIPO_TRAMITE = "solicitud";
	private final static String NOMBRE_TRAMITE = FapProperties.get("fap.aed.procedimientos.tramiterenuncia.nombre");
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.renuncia");
	private final static String BODY_REPORT = "reports/renuncia.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = "renunciaRealizada";
	private final static String JUSTIFICANTE = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroRenuncia");
	private PlatinoGestorDocumentalService platinoGestorDocumentalService;
	
	public TramiteRenuncia(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	/**
	 * Retorna el registro del trámite renuncia
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.renuncia.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteRenuncia.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteRenuncia.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteRenuncia.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteRenuncia.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteRenuncia.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteRenuncia.JUSTIFICANTE;
	}

	@Override
	public String getDescripcionJustificante() {
		return TramiteRenuncia.TIPO_TRAMITE;
	}

	@Override
	public String getTipoTramite() {
		return TramiteRenuncia.TIPO_TRAMITE;
	}

	/**
	 * Retorna los documentos del trámite renuncia
	 */
	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.renuncia.documentos;
	}

	/**
	 * Nombre del fichero del justificante para el trámite renuncia
	 */
	@Override
	public String getPrefijoJustificantePdf() {
		return FapProperties.get("fap.tramitacion.nombreficherojustificante.renuncia");
	}

	/**
	 * Salva el trámite de renuncia
	 */
	@Override
	public void guardar() {
		this.solicitud.renuncia.save();
	}

	/**
	 * Crea el expediente en el AED
	 */
	@Override
	public void crearExpedienteAed() {
		if (!this.solicitud.registro.fasesRegistro.expedienteAed){
			try {
				gestorDocumentalService.crearExpediente(this.solicitud);
				this.solicitud.registro.fasesRegistro.expedienteAed = true;
				this.solicitud.registro.fasesRegistro.save();
			} catch (GestorDocumentalServiceException e) {
				play.Logger.debug("Error creando el expediente en el Gestor Documental", e.getMessage());
				Messages.error("Error creando el expediente en el Gestor Documental");
			}
		}
		else {
			play.Logger.debug("El expediente del aed para la solicitud %s ya está creado", this.solicitud.id);
		}

		//Cambiamos el estado de la solicitud
		if (!this.solicitud.estado.equals("iniciada")) {
			this.solicitud.estado = "iniciada";
			this.solicitud.save();
			Mails.enviar(this.getMail(), this.solicitud);
		}
		
	}

	/**
	 * Crea el expediente en el archivo electrónico de platino
	 */
	@Override
	public void crearExpedientePlatino() throws RegistroServiceException {
		
		if (!this.solicitud.registro.fasesRegistro.expedientePlatino){
			try {
				platinoGestorDocumentalService.crearExpediente(this.solicitud.expedientePlatino);

				this.solicitud.registro.fasesRegistro.expedientePlatino = true;
				this.solicitud.registro.fasesRegistro.save();
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
	protected void validarDocumentacion() {
		
		VerificarDocumentacionService verificar = new VerificarDocumentacionService("solicitud", solicitud.documentacion.documentos);
		List<String> condicionadosAutomaticosNoAportados;
		try {
			condicionadosAutomaticosNoAportados = VerificacionFapController.getDocumentosNoAportadosCondicionadosAutomaticos(NOMBRE_TRAMITE, solicitud.id);
			verificar.preparaPresentacionTramite(condicionadosAutomaticosNoAportados);
		} catch (Throwable e) {
			play.Logger.debug("Error validando la documentacion aportada", e.getMessage());
			Messages.error("Error validando la documentacion aportada");
		}
	}

	/**
	 * No realiza cambios de estado
	 */
	@Override
	public
	final void cambiarEstadoSolicitud() {
		
	}

}
