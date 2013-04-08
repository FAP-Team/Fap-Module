package tramitacion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import controllers.fap.VerificacionFapController;

import tramitacion.Documentos;

import platino.DatosRegistro;
import properties.FapProperties;
import emails.Mails;
import enumerado.fap.gen.EstadosSolicitudEnum;
import es.gobcan.platino.servicios.registro.JustificanteRegistro;
import reports.Report;
import services.GestorDocumentalServiceException;
import services.RegistroService;
import services.RegistroServiceException;
import services.VerificarDocumentacionService;
import services.platino.PlatinoGestorDocumentalService;
import messages.Messages;
import models.Documento;
import models.Registro;
import models.SolicitudGenerica;

public abstract class TramiteSolicitud extends TramiteBase {

	private final static String TIPO_TRAMITE = "Solicitud";
	private final static String NOMBRE_TRAMITE = FapProperties.get("fap.aed.procedimientos.tramitesolicitud.nombre");
	private PlatinoGestorDocumentalService platinoGestorDocumentalService;

	public TramiteSolicitud(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	@Override
	public final String getDescripcionJustificante() {
		return "Justificante de registro de la solicitud de "+TramiteSolicitud.TIPO_TRAMITE;
	}

	/**
	 * Crea el expediente en el Gestor Documental
	 */
	@Override
	public void crearExpediente() throws RegistroServiceException{
		
		if(!getRegistro().fasesRegistro.expedienteAed){
			try {
				gestorDocumentalService.crearExpediente(this.solicitud);
			} catch (GestorDocumentalServiceException e) {
				Messages.error("Error al crear el expediente");
				play.Logger.fatal("Error al crear el expediente para la solicitud "+this.solicitud.id+": "+e);
				throw new RegistroServiceException("Error al crear el expediente");
			}
			getRegistro().fasesRegistro.expedienteAed = true;
			getRegistro().fasesRegistro.save();
		}else{
			play.Logger.info("El expediente del aed para la solicitud %s ya está creado", this.solicitud.id);
		}

	}

	/**
	 * Crea el expediente en el archivo electrónico de platino
	 */
	@Override
	public void crearExpedientePlatino() throws RegistroServiceException {
		if (!this.solicitud.registro.fasesRegistro.expedientePlatino){
			try {
				platinoGestorDocumentalService.crearExpediente(this.solicitud.expedientePlatino);

				this.solicitud.registro.fasesRegistro.expedientePlatino = true;
				this.solicitud.registro.fasesRegistro.save();
			} catch (Exception e) {
				Messages.error("Error creando expediente en el gestor documental de platino");
				throw new RegistroServiceException("Error creando expediente en el gestor documental de platino");
			}
		}
		else {
			play.Logger.debug("El expediente de platino para la solicitud %s ya está creado", solicitud.id);
		}
	}

	
	@Override
	public void crearExpedienteAed() {
		
	}
	
	/**
	 *
	 */
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
			condicionadosAutomaticosNoAportados = VerificacionFapController.getDocumentosNoAportadosCondicionadosAutomaticos(NOMBRE_TRAMITE, solicitud.id);
			verificar.preparaPresentacionTramite(condicionadosAutomaticosNoAportados);
		} catch (Throwable e) {
			play.Logger.debug("Error validando la documentacion aportada", e.getMessage());
			Messages.error("Error validando la documentacion aportada");
		}
	}

	/**
	 * Realiza cambios de estado
	 */
	@Override
	public void cambiarEstadoSolicitud() {
		solicitud.estado=EstadosSolicitudEnum.iniciada.name();
		solicitud.save();
	}

}
