package tramitacion;

import java.util.List;

import properties.FapProperties;

import services.RegistroServiceException;
import models.Alegacion;
import models.Documento;
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

	@Override
	public void guardar() {
		this.solicitud.alegaciones.actual.save();
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

	@Override
	public void crearExpedienteAed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearExpedientePlatino() throws RegistroServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void anadirDocumentosSolicitud() {
		// TODO Auto-generated method stub
		
	}

}
