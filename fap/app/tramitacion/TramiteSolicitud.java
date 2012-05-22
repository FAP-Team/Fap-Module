package tramitacion;

import java.util.ArrayList;
import java.util.List;

import tramitacion.Documentos;

import platino.DatosRegistro;
import properties.FapProperties;
import emails.Mails;
import es.gobcan.platino.servicios.registro.JustificanteRegistro;
import services.GestorDocumentalServiceException;
import services.RegistroService;
import services.RegistroServiceException;
import services.platino.PlatinoGestorDocumentalService;
import messages.Messages;
import models.Documento;
import models.Registro;
import models.SolicitudGenerica;

public abstract class TramiteSolicitud extends TramiteBase {

	private final static String TIPO_TRAMITE = "Solicitud";
	private final static String NOMBRE_TRAMITE = FapProperties.get("fap.aed.procedimientos.tramitesolicitud.nombre");
	private PlatinoGestorDocumentalService platinoGestorDocumentalService;

	protected TramiteSolicitud(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	@Override
	protected final String getDescripcionJustificante() {
		return TramiteSolicitud.TIPO_TRAMITE;
	}

	/**
	 * Crea el expediente en el AED
	 */
	@Override
	protected void crearExpedienteAed() {
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
		}

		//Cambiamos el estado de la solicitud
		if (!this.solicitud.estado.equals("iniciada")) {
			this.solicitud.estado = "iniciada";
			this.solicitud.save();
			Mails.enviar(this.getMail(), this.solicitud);
		}
	}

	/**
	 * Crea el expediente en el archivo electrónico de platino
	 */
	@Override
	protected void crearExpedientePlatino() throws RegistroServiceException {
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
	protected final void anadirDocumentosSolicitud() {
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
	protected void validarDocumentacion() {
		// Validar documentos que se deben eXcluir condicionados automáticos
		//

		// play.Logger.info("Documentos Subidos:" +
		// solicitud.documentacion.documentos.size());
		
		//PresentacionService ps = new PresentacionService(NOMBRE_TRAMITE, this.solicitud,
		//		this.solicitud.documentacion.documentos);

		//ps.preparaPresentacionTramite(this.obtenerObligatoriosNoAportadosCondicionadosAutomatico());
	}

	/**
	 * Sobreescribir para obtener las lista de Documentos obligatorios no aportados
	 * @return La lista de documentos y null en caso de no encontrarse ninguno
	 */
	//TODO SMB 2012/05/14 (protected to public)
	public abstract List<String> obtenerObligatoriosNoAportadosCondicionadosAutomatico();

	/**
	 * No realiza cambios de estado
	 */
	@Override
	protected final void cambiarEstadoSolicitud() {}


}
