package tramitacion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import config.InjectorConfig;
import controllers.fap.FirmaController;

import emails.Mails;

import properties.FapProperties;

import reports.Report;
import services.FirmaService;
import services.GestorDocumentalServiceException;
import services.RegistroServiceException;
import services.platino.PlatinoGestorDocumentalService;
import services.VerificarDocumentacionService;
import messages.Messages;
import models.Alegacion;
import models.Aportacion;
import models.Documento;
import models.DocumentoExterno;
import models.Firmante;
import models.Registro;
import models.SolicitudGenerica;

public class TramiteAportacion extends TramiteBase {

	private final static String TIPO_TRAMITE = "aportacion";
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.aportacion.solicitud");
	private final static String BODY_REPORT = "reports/solicitudAportacion.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = FapProperties.get("fap.tramitacion.aportacion.identificadoremail");
	private final static String JUSTIFICANTE = FapProperties.get("fap.aed.tiposdocumentos.aportacion.registro");
	
	public TramiteAportacion(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	/**
	 * Retorna el registro del trámite alegación
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.aportaciones.actual.registro;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteAportacion.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteAportacion.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteAportacion.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteAportacion.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteAportacion.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteAportacion.JUSTIFICANTE;
	}

	@Override
	public String getDescripcionJustificante() {
		return TramiteAportacion.TIPO_TRAMITE;
	}

	@Override
	public String getTipoTramite() {
		return TramiteAportacion.TIPO_TRAMITE;
	}

	/**
	 * Retorna los documentos del trámite alegación
	 */
	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.aportaciones.actual.documentos;
	}

	@Override
	public String getPrefijoJustificantePdf() {
		return FapProperties.get("fap.tramitacion.aportacion.prefijojustificantepdf");
	}
	
	/**
	 * Mueve el trámite actual a la colección de trámites registrados
	 */
	@Override
	public void moverRegistradas() {
		this.solicitud.aportaciones.registradas.add(this.solicitud.aportaciones.actual);
	}
	
	/**
	 * Prepara un nuevo trámite y lo añade a la variable actual
	 */
	@Override
	public void prepararNuevo() {
		this.solicitud.aportaciones.actual = new Aportacion();
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
		
//		if ((this.solicitud.expedientePlatino != null) && (this.solicitud.expedientePlatino.uri != null) && ((!this.solicitud.expedientePlatino.uri.isEmpty()))){
//			this.solicitud.aportaciones.actual.registro.fasesRegistro.expedientePlatino = true;
//			this.solicitud.aportaciones.actual.registro.fasesRegistro.save();
//		}
//		
//		if (!this.solicitud.aportaciones.actual.registro.fasesRegistro.expedientePlatino){
//			try {
//				platinoGestorDocumentalService.crearExpediente(this.solicitud.expedientePlatino);
//
//				this.solicitud.aportaciones.actual.registro.fasesRegistro.expedientePlatino = true;
//				this.solicitud.aportaciones.actual.registro.fasesRegistro.save();
//			} catch (Exception e) {
//				Messages.error("Error creando expediente en el gestor documental de platino");
//				throw new RegistroServiceException("Error creando expediente en el gestor documental de platino");
//			}
//		}
//		else {
//			play.Logger.debug("El expediente de platino para la solicitud %s ya está creado", solicitud.id);
//		}
	}

	@Override
	public List<DocumentoExterno> getDocumentosExternos() {
		// TODO Auto-generated method stub
		return new ArrayList<DocumentoExterno>();
	}

    @Override
    public void validar() {
        validarDocumentacion();
    }

    protected void validarDocumentacion() {
        VerificarDocumentacionService.comprobarFirmasDocumentos(this.getDocumentos());
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
