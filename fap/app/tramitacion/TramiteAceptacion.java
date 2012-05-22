//package tramitacion;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import services.RegistroServiceException;
//import tramitacion.Documentos;
//
//import platino.DatosRegistro;
//import properties.FapProperties;
//import emails.Mails;
//import es.gobcan.platino.servicios.registro.JustificanteRegistro;
//import messages.Messages;
//import models.Documento;
//import models.Registro;
//import models.SolicitudGenerica;
//
//public class TramiteAceptacion extends TramiteBase {
//
//	private final static String TIPO_TRAMITE = "Aceptación";
//	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.aceptacion");
//	private final static String BODY_REPORT = "reports/aceptacion.html";
//	private final static String HEADER_REPORT = "reports/header.html";
//	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
//	private final static String MAIL = FapProperties.get("fap.tramitacion.aceptacion.identificadoremail");
//	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroAceptacion");
//	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.aceptacion.prefijojustificantepdf");
//	
//	public TramiteAceptacion(SolicitudGenerica solicitud) {
//		super(solicitud);
//	}
//	
//	@Override
//	public String getTipoTramite() {
//		return TramiteAceptacion.TIPO_TRAMITE;
//	}
//
//	@Override
//	protected String getTipoRegistro() {
//		return TramiteAceptacion.TIPO_REGISTRO;
//	}
//
//	@Override
//	protected String getBodyReport() {
//		return TramiteAceptacion.BODY_REPORT;
//	}
//	
//	@Override
//	protected String getHeaderReport() {
//		return TramiteAceptacion.HEADER_REPORT;
//	}
//	
//	@Override
//	protected String getFooterReport() {
//		return TramiteAceptacion.FOOTER_REPORT;
//	}
//
//	@Override
//	protected String getMail() {
//		return TramiteAceptacion.MAIL;
//	}
//	
//	@Override
//	protected String getJustificanteRegistro() {
//		return TramiteAceptacion.JUSTIFICANTE_REGISTRO;
//	}
//	
//	@Override
//	protected final String getDescripcionJustificante() {
//		return TramiteAceptacion.TIPO_TRAMITE;
//	}
//	
//	/**
//	 * Retorna el registro del trámite aceptación
//	 */
//	@Override
//	public Registro getRegistro() {
//		return this.solicitud.aceptacion.registro;
//	}
//
//	/**
//	 * Retorna los documentos del trámite aceptación
//	 */
//	@Override
//	public List<Documento> getDocumentos() {
//		return this.solicitud.aceptacion.documentos;
//	}
//	
//	/**
//	 * Nombre del fichero del justificante para el trámite aceptación
//	 */
//	@Override
//	protected String getPrefijoJustificantePdf() {
//		return TramiteAceptacion.PREFIJO_JUSTIFICANTE_PDF;
//	}
//	
//	/**
//	 * Salva el trámite de aceptación
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
//}
