//package tramitacion;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import tramitacion.Documentos;
//
//import platino.DatosRegistro;
//import properties.FapProperties;
//import emails.Mails;
//import es.gobcan.platino.servicios.registro.JustificanteRegistro;
//import services.RegistroServiceException;
//import messages.Messages;
//import models.Documento;
//import models.Registro;
//import models.SolicitudGenerica;
//
//public class TramiteRenuncia extends TramiteBase {
//
//	private final static String TIPO_TRAMITE = "Renuncia";
//	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.renuncia");
//	private final static String BODY_REPORT = "reports/renuncia.html";
//	private final static String HEADER_REPORT = "reports/header.html";
//	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
//	private final static String MAIL = FapProperties.get("fap.tramitacion.renuncia.identificadoremail");
//	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroRenuncia");
//	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.renuncia.prefijojustificantepdf");
//	
//	public TramiteRenuncia(SolicitudGenerica solicitud) {
//		super(solicitud);
//	}
//	
//	@Override
//	public String getTipoTramite() {
//		return TramiteRenuncia.TIPO_TRAMITE;
//	}
//	
//	@Override
//	protected String getTipoRegistro() {
//		return TramiteRenuncia.TIPO_REGISTRO;
//	}
//
//	@Override
//	protected String getBodyReport() {
//		return TramiteRenuncia.BODY_REPORT;
//	}
//	
//	@Override
//	protected String getHeaderReport() {
//		return TramiteRenuncia.HEADER_REPORT;
//	}
//	
//	@Override
//	protected String getFooterReport() {
//		return TramiteRenuncia.FOOTER_REPORT;
//	}
//
//	@Override
//	protected String getMail() {
//		return TramiteRenuncia.MAIL;
//	}
//	
//	@Override
//	protected String getJustificanteRegistro() {
//		return TramiteRenuncia.JUSTIFICANTE_REGISTRO;
//	}
//	
//	@Override
//	protected final String getDescripcionJustificante() {
//		return TramiteRenuncia.TIPO_TRAMITE;
//	}
//	
//	/**
//	 * Retorna el registro del trámite renuncia
//	 */
//	@Override
//	public Registro getRegistro() {
//		return this.solicitud.aceptacion.registro;
//	}
//
//	/**
//	 * Retorna los documentos del trámite renuncia
//	 */
//	@Override
//	public List<Documento> getDocumentos() {
//		return this.solicitud.aceptacion.documentos;
//	}
//	
//	/**
//	 * Nombre del fichero del justificante para el trámite renuncia
//	 */
//	@Override
//	protected String getPrefijoJustificantePdf() {
//		return PREFIJO_JUSTIFICANTE_PDF;
//	}
//
//	/**
//	 * Salva el trámite de renuncia
//	 */
//	@Override
//	protected void guardar() {
//		this.solicitud.aceptacion.save();
//	}
//	
//	@Override
//	protected final void crearExpedienteAed() {
//	}
//	
//	@Override
//	protected final void crearExpedientePlatino() throws RegistroServiceException {
//		
//	}
//	
//	@Override
//	protected final void anadirDocumentosSolicitud() {
//		this.solicitud.documentacion.documentos.addAll(this.getDocumentos());
//	}
//
//
//}
