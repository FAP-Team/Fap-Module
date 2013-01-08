package tramitacion;

import java.io.File;
import java.util.List;

import emails.Mails;

import properties.FapProperties;

import reports.Report;
import services.GestorDocumentalServiceException;
import services.RegistroServiceException;
import services.platino.PlatinoGestorDocumentalService;
import messages.Messages;
import models.Alegacion;
import models.Documento;
import models.DocumentoExterno;
import models.Registro;
import models.SolicitudGenerica;

public class TramiteAlegacion extends TramiteBase {

	private final static String TIPO_TRAMITE = "solicitud";
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.alegacion");
	private final static String BODY_REPORT = "reports/alegacion.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = "alegacionRealizada";
	private final static String JUSTIFICANTE = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroAlegacion");
	private PlatinoGestorDocumentalService platinoGestorDocumentalService;
	
	public TramiteAlegacion(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	/**
	 * Retorna el registro del trámite alegación
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.alegaciones.actual.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteAlegacion.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteAlegacion.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteAlegacion.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteAlegacion.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteAlegacion.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteAlegacion.JUSTIFICANTE;
	}

	@Override
	public String getDescripcionJustificante() {
		return TramiteAlegacion.TIPO_TRAMITE;
	}

	@Override
	public String getTipoTramite() {
		return TramiteAlegacion.TIPO_TRAMITE;
	}

	/**
	 * Retorna los documentos del trámite alegación
	 */
	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.alegaciones.actual.documentos;
	}

	@Override
	public String getPrefijoJustificantePdf() {
		return FapProperties.get("fap.tramitacion.alegacion.prefijojustificantepdf");
	}
	
	/**
	 * Mueve el trámite actual a la colección de trámites registrados
	 */
	public void moverRegistradas() {
		this.solicitud.alegaciones.alegacionRegistradas.add(this.solicitud.alegaciones.actual);
	}
	
	/**
	 * Prepara un nuevo trámite y lo añade a la variable actual
	 */
	public void prepararNuevo() {
		this.solicitud.alegaciones.actual = new Alegacion();
	}

	@Override
	public void validarReglasConMensajes() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Crea el expediente en el AED
	 */
	@Override
	public void crearExpedienteAed() {
		if (!this.solicitud.alegaciones.actual.registro.fasesRegistro.expedienteAed){
			try {
				gestorDocumentalService.crearExpediente(this.solicitud);
				this.solicitud.alegaciones.actual.registro.fasesRegistro.expedienteAed = true;
				this.solicitud.alegaciones.actual.registro.fasesRegistro.save();
			} catch (GestorDocumentalServiceException e) {
				play.Logger.debug("Error creando el expediente en el Gestor Documental", e.getMessage());
				Messages.error("Error creando el expediente en el Gestor Documental");
			}
		}
		else {
			play.Logger.debug("El expediente del aed para la solicitud %s ya está creado", this.solicitud.alegaciones.actual.id);
		}

		if (!this.solicitud.alegaciones.actual.estado.equals("iniciada")) {
			Mails.enviar(this.getMail(), this.solicitud);
		}
		
	}

	/**
	 * Crea el expediente en el archivo electrónico de platino
	 */
	@Override
	public void crearExpedientePlatino() throws RegistroServiceException {
		
		if (!this.solicitud.alegaciones.actual.registro.fasesRegistro.expedientePlatino){
			try {
				platinoGestorDocumentalService.crearExpediente(this.solicitud.expedientePlatino);

				this.solicitud.alegaciones.actual.registro.fasesRegistro.expedientePlatino = true;
				this.solicitud.alegaciones.actual.registro.fasesRegistro.save();
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
	public List<DocumentoExterno> getDocumentosExternos() {
		// TODO Auto-generated method stub
		return solicitud.alegaciones.actual.documentosExternos;
	}

	@Override
	public boolean hanFirmadoTodos() {
		// TODO Auto-generated method stub
		return registro.firmantes.hanFirmadoTodos();
	}

}
