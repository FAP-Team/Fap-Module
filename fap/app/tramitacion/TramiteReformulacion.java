//package tramitacion;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import tramitacion.Documentos;
//
//import platino.DatosRegistro;
//import properties.FapProperties;
//import emails.Mails;
//import es.gobcan.platino.servicios.registro.JustificanteRegistro;
//import services.RegistroServiceException;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;
//import messages.Messages;
//import models.Documento;
//import models.Registro;
//import models.SolicitudGenerica;
//
//public class TramiteReformulacion extends TramiteBase {
//
//	private final static String TIPO_TRAMITE = "Reformulación";
//	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.reformulacion");
//	private final static String BODY_REPORT = "reports/reformulacion.html";
//	private final static String HEADER_REPORT = "reports/header.html";
//	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
//	private final static String MAIL = FapProperties.get("fap.tramitacion.reformulacion.identificadoremail");
//	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroReformulacion");
//	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.reformulacion.prefijojustificantepdf");
//	
//	public TramiteReformulacion(SolicitudGenerica solicitud) {
//		super(solicitud);
//	}
//	
//	@Override
//	public String getTipoTramite() {
//		return TramiteReformulacion.TIPO_TRAMITE;
//	}
//	
//	@Override
//	protected String getTipoRegistro() {
//		return TramiteReformulacion.TIPO_REGISTRO;
//	}
//
//	@Override
//	protected String getBodyReport() {
//		return TramiteReformulacion.BODY_REPORT;
//	}
//	
//	@Override
//	protected String getHeaderReport() {
//		return TramiteReformulacion.HEADER_REPORT;
//	}
//	
//	@Override
//	protected String getFooterReport() {
//		return TramiteReformulacion.FOOTER_REPORT;
//	}
//
//	@Override
//	protected String getMail() {
//		return TramiteReformulacion.MAIL;
//	}
//	
//	@Override
//	protected String getJustificanteRegistro() {
//		return TramiteReformulacion.JUSTIFICANTE_REGISTRO;
//	}
//	
//	@Override
//	protected final String getDescripcionJustificante() {
//		return TramiteReformulacion.TIPO_TRAMITE;
//	}
//	
//	/**
//	 * Retorna el registro del trámite reformulación
//	 */
//	@Override
//	public Registro getRegistro() {
//		return this.solicitud.reformulacion.registro;
//	}
//
//	/**
//	 * Retorna los documentos del trámite reformulación
//	 */
//	@Override
//	public List<Documento> getDocumentos() {
//		return this.solicitud.reformulacion.documentos;
//	}
//	
//	/**
//	 * Nombre del fichero del justificante para el trámite reformulación
//	 */
//	@Override
//	protected String getPrefijoJustificantePdf() {
//		return PREFIJO_JUSTIFICANTE_PDF;
//	}
//
//	/**
//	 * Salva el trámite de reformulación
//	 */
//	@Override
//	protected void guardar() {
//		this.solicitud.reformulacion.save();
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
