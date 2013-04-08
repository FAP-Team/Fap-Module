package tramitacion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import emails.Mails;
import enumerado.fap.gen.EstadoConvocatoriaEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;

import properties.FapProperties;

import models.Agente;
import models.Convocatoria;
import models.Documento;
import models.DocumentoExterno;
import models.Firmante;
import models.JsonPeticionModificacion;
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import reports.Report;
import services.GestorDocumentalServiceException;
import services.RegistroServiceException;
import services.VerificarDocumentacionService;
import utils.PeticionModificacion;
import utils.PeticionModificacion.ValorCampoModificado;

import messages.Messages;

public class TramiteSolicitudModificacionFap extends TramiteSolicitud {
	
	private final static String TIPO_TRAMITE = FapProperties.get("fap.tramitacion.tramite.modificacion.tipo");
	private final static String TIPO_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.solicitud.modificacion");
	private final static String BODY_REPORT = "reports/solicitudModificacion.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAIL = "solicitudModificada";
	private final static String JUSTIFICANTE_REGISTRO = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitudModificacion");
	private final static String PREFIJO_JUSTIFICANTE_PDF = FapProperties.get("fap.tramitacion.prefijojustificantepdf.solicitudModificacion");
	
	public TramiteSolicitudModificacionFap(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	@Override
	public String getTipoTramite() {
		return TramiteSolicitudModificacionFap.TIPO_TRAMITE;
	}

	@Override
	public String getTipoRegistro() {
		return TramiteSolicitudModificacionFap.TIPO_REGISTRO;
	}

	@Override
	public String getBodyReport() {
		return TramiteSolicitudModificacionFap.BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return TramiteSolicitudModificacionFap.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteSolicitudModificacionFap.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		return TramiteSolicitudModificacionFap.MAIL;
	}

	@Override
	public String getJustificanteRegistro() {
		return TramiteSolicitudModificacionFap.JUSTIFICANTE_REGISTRO;
	}

	/**
	 * Retorna el registro de la solicitud
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.registroModificacion.get(this.solicitud.registroModificacion.size()-1).registro;
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
	
	@Override
	public void validarReglasConMensajes() {
	}
	
	@Override
	public File generarBorrador(){
    	File borrador = null;
        borrador = new File (this.getBodyReport());
        if(!Messages.hasErrors()){
            try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
            	RegistroModificacion registroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
            	List<ValorCampoModificado> valoresModificacion = new ArrayList<ValorCampoModificado>();
            	PeticionModificacion peticionModificacion;
        		Gson gson = new Gson();
        		for (JsonPeticionModificacion json: registroModificacion.jsonPeticionesModificacion){
        			peticionModificacion = gson.fromJson(json.jsonPeticion, PeticionModificacion.class);
        			valoresModificacion.addAll(peticionModificacion.valoresModificado);
        		}
        		
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("valoresModificacion", valoresModificacion);
            	borrador = new Report(this.getBodyReport()).header(this.getHeaderReport()).footer(this.getFooterReport()).renderTmpFile(solicitud, valoresModificacion);
                registro.borrador = new Documento();
                registro.borrador.tipo = getTipoRegistro();
                registro.save();
            } catch (Exception ex2) {
                Messages.error("Error generando el documento borrador");
                play.Logger.error("Error generando el documento borrador: "+ex2.getMessage());
            }
        }

        return borrador;
    }
    
	@Override
    public File generarOficial(){
        File oficial = null;
        if(!Messages.hasErrors()){
            try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
            	RegistroModificacion registroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
            	List<ValorCampoModificado> valoresModificacion = new ArrayList<ValorCampoModificado>();
            	PeticionModificacion peticionModificacion;
        		Gson gson = new Gson();
        		for (JsonPeticionModificacion json: registroModificacion.jsonPeticionesModificacion){
        			peticionModificacion = gson.fromJson(json.jsonPeticion, PeticionModificacion.class);
        			valoresModificacion.addAll(peticionModificacion.valoresModificado);
        		}
        		
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("valoresModificacion", valoresModificacion);
            	oficial = new Report(this.getBodyReport()).header(this.getHeaderReport()).registroSize().renderTmpFile(solicitud, valoresModificacion);
                registro.oficial = new Documento();
                registro.oficial.tipo = getTipoRegistro();
                registro.save();
            } catch (Exception ex2) {
                Messages.error("Error generando el documento oficial");
                play.Logger.error("Error generando el documento oficial: "+ex2.getMessage());
            }
        }
        return oficial;
    }
	
	/**
	 * Realiza cambios de estado
	 */
	@Override
	public void cambiarEstadoSolicitud() {
		solicitud.estado=EstadosSolicitudEnum.iniciada.name();
		solicitud.activoModificacion=false;
		solicitud.save();
	}
	
	/**
	 * Crea el expediente en el AED
	 */
	@Override
	public void crearExpedienteAed() {
		if (!this.solicitud.registro.fasesRegistro.expedienteAed){
			try {
				gestorDocumentalService.crearExpediente(this.solicitud);
				this.solicitud.registro.fasesRegistro.expedienteAed = true;
				this.solicitud.registro.fasesRegistro.save();
			} catch (GestorDocumentalServiceException e) {
				play.Logger.debug("Error creando el expediente en el Gestor Documental", e.getMessage());
				Messages.error("Error creando el expediente en el Gestor Documental");
			}
		}
		else {
			play.Logger.debug("El expediente del aed para la solicitud %s ya está creado", this.solicitud.id);
			this.registro.fasesRegistro.expedienteAed = true;
			this.registro.fasesRegistro.save();
		}
	}

}
