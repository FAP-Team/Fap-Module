package tramitacion;

import java.util.List;

import enumerado.fap.gen.EstadosSolicitudEnum;

import properties.FapProperties;
import services.RegistroServiceException;

import models.Documento;
import models.DocumentoExterno;
import models.Registro;
import models.SolicitudGenerica;

public class TramiteDesistimiento extends TramiteBase {

	private final static String TIPO_TRAMITE = "Solicitud Estancia";
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.desistimiento");
	private final static String BODY_REPORT = "reports/desistimiento.html"; //Solicitud.html?
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = "DesistimientoRealizado";
	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.prefijojustificantepdf.solicitud");
	
	public TramiteDesistimiento(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	/**
	 * Retorna el registro de la solicitud de estancia
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.desistimiento.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteDesistimiento.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteDesistimiento.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteDesistimiento.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteDesistimiento.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteDesistimiento.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteDesistimiento.JUSTIFICANTE_REGISTRO;
	}

	@Override
	public String getTipoTramite() {
		return TramiteDesistimiento.TIPO_TRAMITE;
	}

	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.desistimiento.documentos;
	}

	@Override
	public String getPrefijoJustificantePdf(){
		return PREFIJO_JUSTIFICANTE_PDF;
	}

	@Override
	public String getDescripcionJustificante() {
		return TramiteDesistimiento.TIPO_TRAMITE;
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

	/**
	 * Cambia el estado de la solicitud a "Desistido"
	 */
	@Override
	public	final void cambiarEstadoSolicitud() {
		this.solicitud.estado = EstadosSolicitudEnum.desistido.name();
	}

	@Override
	public List<DocumentoExterno> getDocumentosExternos() {
		// TODO Auto-generated method stub
		return solicitud.desistimiento.documentosExternos;
	}

	@Override
	public boolean hanFirmadoTodos() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
