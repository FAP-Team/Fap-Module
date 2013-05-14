package tramitacion;

import java.util.ArrayList;
import java.util.List;

import enumerado.fap.gen.EstadoConvocatoriaEnum;

import properties.FapProperties;

import models.Agente;
import models.Convocatoria;
import models.Documento;
import models.DocumentoExterno;
import models.Firmante;
import models.Firmantes;
import models.Registro;
import models.SolicitudGenerica;
import services.VerificarDocumentacionService;

import messages.Messages;

public class TramiteSolicitudFap extends TramiteSolicitud {
	
	private final static String TIPO_TRAMITE = FapProperties.get("fap.tramitacion.tramite.tipo");
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
	private final static String BODY_REPORT = "reports/solicitud.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = FapProperties.get("fap.tramitacion.iniciada.identificadoremail");
	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.prefijojustificantepdf.solicitud");
	
	public TramiteSolicitudFap(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	@Override
	public String getTipoTramite() {
		return TramiteSolicitudFap.TIPO_TRAMITE;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteSolicitudFap.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteSolicitudFap.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteSolicitudFap.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteSolicitudFap.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteSolicitudFap.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteSolicitudFap.JUSTIFICANTE_REGISTRO;
	}

	/**
	 * Retorna el registro de la solicitud
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.registro;
	}

	/**
	 * Retorna los documentos de la solicitud
	 */
	@Override
	public List<Documento> getDocumentos() {
		// TODO Auto-generated method stub
		return this.solicitud.documentacion.documentos;
	}
	
	/**
	 * Retorna los documentos externos de la solicitud
	 */
	@Override
	public List<DocumentoExterno> getDocumentosExternos() {
		// TODO Auto-generated method stub
		return this.solicitud.documentacion.documentosExternos;
	}

	/**
	 * Nombre del fichero del justificante para la solicitud
	 */
	@Override
	public String getPrefijoJustificantePdf() {
		return PREFIJO_JUSTIFICANTE_PDF;
	}

	/**
	 *
	 * @return
	 */
	public List<String> obtenerObligatoriosNoAportadosCondicionadosAutomatico() {

		return null;
	}

	/**
	 *
	 * @return true firmado por representante o funcionario habilitado
	 */
	private boolean esRequeridoPoderRepresentante(){

		return false;
	}
	
	@Override
	public void validarReglasConMensajes() {
		this.validarDocumentacion();
	}

	/**
	 * Validar los documentos condicionados automaticamente
	 */
	public void validarDocumentacion() {
		
		VerificarDocumentacionService verificar = new VerificarDocumentacionService("solicitud", this.getDocumentos(), this.getDocumentosExternos());
		List<String> condicionadosAutomaticosNoAportados;
		try {
			condicionadosAutomaticosNoAportados = obtenerObligatoriosNoAportadosCondicionadosAutomatico();
			verificar.preparaPresentacionTramite(condicionadosAutomaticosNoAportados);
		} catch (Throwable e) {
			play.Logger.debug("Error validando la documentacion aportada", e.getMessage());
			Messages.error("Error validando la documentacion aportada");
		}
	}

	@Override
	public boolean hanFirmadoTodos() {
		// TODO Auto-generated method stub
		return registro.firmantes.hanFirmadoTodos();
	}

}
