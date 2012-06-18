package tramitacion;

import java.util.List;

import models.*;
import tramitacion.TramiteSolicitud;

public class TramiteAplicacion extends TramiteSolicitud{
	
	private final static String TIPO_TRAMITE = "Solicitud Aplicacion";
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
	private final static String BODY_REPORT = "reports/solicitud.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = "solicitudIniciada";
	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.prefijojustificantepdf.solicitud");
	
	public TramiteAplicacion(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	@Override
	public Registro getRegistro() {
		return this.solicitud.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteSolicitudEstancia.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteSolicitudEstancia.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteSolicitudEstancia.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteSolicitudEstancia.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteSolicitudEstancia.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteSolicitudEstancia.JUSTIFICANTE_REGISTRO;
	}

	@Override
	public String getTipoTramite() {
		return TramiteSolicitudEstancia.TIPO_TRAMITE;
	}

	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.documentacion.documentos;
	}

	@Override
	public String getPrefijoJustificantePdf() {
		return PREFIJO_JUSTIFICANTE_PDF;
	}

	@Override
	public void guardar() {
		this.solicitud.save();
	}
	
}
