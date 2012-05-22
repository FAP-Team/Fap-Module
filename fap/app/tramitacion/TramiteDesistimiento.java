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
//import enumerado.fap.gen.EstadosSolicitudEnum;
//import es.gobcan.platino.servicios.registro.JustificanteRegistro;
//import services.RegistroServiceException;
//import messages.Messages;
//import models.Documento;
//import models.Registro;
//import models.SolicitudGenerica;
//
//public class TramiteDesistimiento extends TramiteBase {
//
//	private final static String TIPO_TRAMITE = "Desistimiento";
//	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.desistimiento");
//	private final static String BODY_REPORT = "reports/desistimiento.html";
//	private final static String HEADER_REPORT = "reports/header.html";
//	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
//	private final static String MAIL = FapProperties.get("fap.tramitacion.desistimiento.identificadoremail");
//	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroDesistimiento");
//	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.desistimiento.prefijojustificantepdf");
//	
//	public TramiteDesistimiento(SolicitudGenerica solicitud) {
//		super(solicitud);
//	}
//	
//	@Override
//	public String getTipoTramite() {
//		return TramiteDesistimiento.TIPO_TRAMITE;
//	}
//	
//	@Override
//	protected String getTipoRegistro() {
//		return TramiteDesistimiento.TIPO_REGISTRO;
//	}
//
//	@Override
//	protected String getBodyReport() {
//		return TramiteDesistimiento.BODY_REPORT;
//	}
//	
//	@Override
//	protected String getHeaderReport() {
//		return TramiteDesistimiento.HEADER_REPORT;
//	}
//	
//	@Override
//	protected String getFooterReport() {
//		return TramiteDesistimiento.FOOTER_REPORT;
//	}
//
//	@Override
//	protected String getMail() {
//		return TramiteDesistimiento.MAIL;
//	}
//	
//	@Override
//	protected String getJustificanteRegistro() {
//		return TramiteDesistimiento.JUSTIFICANTE_REGISTRO;
//	}
//	
//	@Override
//	protected final String getDescripcionJustificante() {
//		return TramiteDesistimiento.TIPO_TRAMITE;
//	}
//	
//	/**
//	 * Retorna el registro del trámite desistimiento
//	 */
//	@Override
//	public Registro getRegistro() {
//		return this.solicitud.desistimiento.registro;
//	}
//
//	/**
//	 * Retorna los documentos del trámite desistimiento
//	 */
//	@Override
//	public List<Documento> getDocumentos() {
//		return this.solicitud.desistimiento.documentos;
//	}
//	
//	/**
//	 * Nombre del fichero del justificante para el trámite desistimiento
//	 */
//	@Override
//	protected String getPrefijoJustificantePdf() {
//		return PREFIJO_JUSTIFICANTE_PDF;
//	}
//
//	/**
//	 * Salva el trámite de desistimiento
//	 */
//	@Override
//	protected void guardar() {
//		this.solicitud.desistimiento.save();
//	}
//	
//	/**
//	 * Cambia el estado de la solicitud a "Desistido"
//	 */
//	@Override
//	protected final void cambiarEstadoSolicitud() {
//		this.solicitud.estado = EstadosSolicitudEnum.desistido.name();
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
//}
