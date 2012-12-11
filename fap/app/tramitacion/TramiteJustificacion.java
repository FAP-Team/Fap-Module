package tramitacion;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.Documento;
import models.DocumentoExterno;
import models.Firmante;
import models.Justificacion;
import models.Registro;
import models.SolicitudGenerica;

import org.joda.time.DateTime;

import properties.FapProperties;
import services.FirmaService;
import services.RegistroServiceException;
import config.InjectorConfig;
import controllers.fap.FirmaController;

public class TramiteJustificacion extends TramiteBase{

	private final static String TIPO_TRAMITE = "justificacion";
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificacion.solicitud");
	private final static String BODY_REPORT = "reports/solicitudJustificacion.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = "justificacionRealizada";
	private final static String JUSTIFICANTE = FapProperties.get("fap.aed.tiposdocumentos.justificacion.registro");
	
	public TramiteJustificacion(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	/**
	 * Retorna el registro del trámite Justificacion
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.justificaciones.actual.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteJustificacion.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteJustificacion.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteJustificacion.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteJustificacion.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteJustificacion.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteJustificacion.JUSTIFICANTE;
	}

	@Override
	public String getDescripcionJustificante() {
		return TramiteJustificacion.TIPO_TRAMITE;
	}

	@Override
	public String getTipoTramite() {
		return TramiteJustificacion.TIPO_TRAMITE;
	}

	/**
	 * Retorna los documentos del trámite alegación
	 */
	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.justificaciones.actual.documentos;
	}

	@Override
	public String getPrefijoJustificantePdf() {
		return FapProperties.get("fap.tramitacion.justificacion.prefijojustificantepdf");
	}
	
	/**
	 * Mueve el trámite actual a la colección de trámites registrados
	 */
	@Override
	public void moverRegistradas() {
		this.solicitud.justificaciones.registradas.add(this.solicitud.justificaciones.actual);
	}
	
	/**
	 * Prepara un nuevo trámite y lo añade a la variable actual
	 */
	@Override
	public void prepararNuevo() {
		this.solicitud.justificaciones.actual = new Justificacion();
	}

	@Override
	public void validarReglasConMensajes() {
		// TODO Auto-generated method stub
	}

	/**
	 * Crea el expediente en el archivo electrónico de platino
	 */
	@Override
	public void crearExpedientePlatino() throws RegistroServiceException {
		
	}

	@Override
	public List<DocumentoExterno> getDocumentosExternos() {
		// TODO Auto-generated method stub
		return new ArrayList<DocumentoExterno>();
	}
	
	@Override
	public void firmar(String firma){
        if(registro.fasesRegistro.borrador && !registro.fasesRegistro.firmada){
            String identificadorFirmante = FirmaController.getIdentificacionFromFirma(firma);
            if(registro.firmantes.containsFirmanteConIdentificador(identificadorFirmante) && !registro.firmantes.haFirmado(identificadorFirmante)){
            	FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
            	Firmante firmante = firmaService.getFirmante(firma, registro.oficial);
            	for (Firmante firmanteAux: registro.firmantes.todos){
            		if (firmanteAux.idvalor.equals(identificadorFirmante)){
            			firmante.cardinalidad = firmanteAux.cardinalidad;
            			firmante.tipo = firmanteAux.tipo;
            			registro.firmantes.todos.remove(firmanteAux);
            			registro.firmantes.todos.add(firmante);
            			registro.save();
            			break;
            		}
            	}
            	firmante.fechaFirma = new DateTime();
                almacenarFirma(firma, registro.oficial, firmante);
                firmante.save();
                avanzarFaseFirmada();
            } else if (registro.firmantes.haFirmado(identificadorFirmante)){
            	play.Logger.error("La solicitud ya ha sido firmada por ese certificado");
                Messages.error("La solicitud ya ha sido firmada por ese certificado");
            } else {
            	String firmantes="{";
            	for (Firmante firmante: registro.firmantes.todos){
            		firmantes+=firmante.toString()+" | ";
            	}
            	firmantes+="}";
            	play.Logger.error("El certificado <"+identificadorFirmante+"> no se corresponde con uno que debe firmar la solicitud: "+firmantes);
                Messages.error("El certificado no se corresponde con uno que debe firmar la solicitud");
            }
        }
    }

	@Override
	public void crearExpedienteAed() {
		
	}
}
