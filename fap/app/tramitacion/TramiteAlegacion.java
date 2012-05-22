//package tramitacion;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.log4j.Logger;
//
//import tramitacion.Documentos;
//
//import platino.DatosRegistro;
//import properties.FapProperties;
//import services.RegistroService;
//import services.RegistroServiceException;
//import emails.Mails;
//import es.gobcan.platino.servicios.registro.JustificanteRegistro;
//import messages.Messages;
//import models.Documento;
//import models.Registro;
//import models.SolicitudGenerica;
//
//public class TramiteAlegacion extends TramiteBase {
//
//	private final static String TIPO_TRAMITE = "Alegación";
//	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.alegacion");
//	private final static String BODY_REPORT = "reports/alegacion.html";
//	private final static String HEADER_REPORT = "reports/header.html";
//	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
//	private final static String MAIL = FapProperties.get("fap.tramitacion.alegacion.identificadoremail");
//	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroAlegacion");
//	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.alegacion.prefijojustificantepdf");
//
//	public TramiteAlegacion(SolicitudGenerica solicitud) {
//		super(solicitud);
//	}
//	
//	@Override
//	public String getTipoTramite() {
//		return TramiteAlegacion.TIPO_TRAMITE;
//	}
//	
//	@Override
//	protected String getTipoRegistro() {
//		return TramiteAlegacion.TIPO_REGISTRO;
//	}
//
//	@Override
//	protected String getBodyReport() {
//		return TramiteAlegacion.BODY_REPORT;
//	}
//	
//	@Override
//	protected String getHeaderReport() {
//		return TramiteAlegacion.HEADER_REPORT;
//	}
//	
//	@Override
//	protected String getFooterReport() {
//		return TramiteAlegacion.FOOTER_REPORT;
//	}
//
//	@Override
//	protected String getMail() {
//		return TramiteAlegacion.MAIL;
//	}
//	
//	@Override
//	protected String getJustificanteRegistro() {
//		return TramiteAlegacion.JUSTIFICANTE_REGISTRO;
//	}
//	
//	@Override
//	protected final String getDescripcionJustificante() {
//		return TramiteAlegacion.TIPO_TRAMITE;
//	}
//
//	/**
//	 * Retorna el registro del trámite alegación
//	 */
//	@Override
//	public Registro getRegistro() {
//		return this.solicitud.alegaciones.actual.registro;
//	}
//
//	/**
//	 * Retorna los documentos del trámite alegación
//	 */
//	@Override
//	public List<Documento> getDocumentos() {
//		return this.solicitud.alegaciones.actual.documentos;
//	}
//	
//	/**
//	 * Nombre del fichero del justificante para el trámite alegación
//	 */
//	@Override
//	protected String getPrefijoJustificantePdf() {
//		return TramiteAlegacion.PREFIJO_JUSTIFICANTE_PDF;
//	}
//
//	/**
//	 * Salva el trámite de alegación
//	 */
//	@Override
//	protected void guardar() {
//		this.solicitud.alegaciones.actual.save();
//	}
//
//	/**
//	 * Mueve la alegación actual a las alegaciones registradas
//	 */
//	@Override
//	protected void moverRegistradas() {
//		solicitud.alegaciones.alegacionRegistradas.add(this.solicitud.alegaciones.actual);
//	}
//	
//	/**
//	 * Prepara un nuevo trámite de alegación y lo añade a la variable actual
//	 */
//	@Override
//	protected void prepararNuevo() {
//		solicitud.alegaciones.actual = new Alegacion();
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
