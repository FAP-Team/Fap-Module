package tramitacion;

import java.util.List;

import properties.FapProperties;
import services.VerificarDocumentacionService;

import messages.Messages;
import models.Documento;
import models.DocumentoExterno;
import models.Registro;
import models.SolicitudGenerica;

public class TramiteSolicitudApp extends TramiteSolicitud {

	private final static String TIPO_TRAMITE = "Solicitud";
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
	private final static String BODY_REPORT = "reports/solicitud.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = "solicitudIniciada";
	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.prefijojustificantepdf.solicitud");
	
	public TramiteSolicitudApp(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	@Override
	public Registro getRegistro() {
		return this.solicitud.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteSolicitudApp.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteSolicitudApp.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteSolicitudApp.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteSolicitudApp.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteSolicitudApp.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteSolicitudApp.JUSTIFICANTE_REGISTRO;
	}

	@Override
	public String getTipoTramite() {
		return TramiteSolicitudApp.TIPO_TRAMITE;
	}

	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.documentacion.documentos;
	}

	@Override
	public List<DocumentoExterno> getDocumentosExternos() {
		return this.solicitud.documentacion.documentosExternos;
	}

	@Override
	public String getPrefijoJustificantePdf() {
		return TramiteSolicitudApp.PREFIJO_JUSTIFICANTE_PDF;
	}
	
	@Override
	public void validarReglasConMensajes() {
		this.validarDocumentacion();
	}

	/**
	 * Validar los documentos condicionados automaticamente
	 */
	public void validarDocumentacion() {
		
	}
	
}
