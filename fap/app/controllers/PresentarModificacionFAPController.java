package controllers;

import messages.Messages;
import messages.Messages.MessageType;
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import play.mvc.Util;
import tramitacion.TramiteBase;
import controllers.fap.PresentacionFapController;
import controllers.fap.PresentacionModificacionFapController;
import controllers.gen.PresentarModificacionFAPControllerGen;

public class PresentarModificacionFAPController extends PresentarModificacionFAPControllerGen {

	
	@Util
	public static RegistroModificacion getRegistroModificacion(Long idSolicitud, Long idRegistroModificacion) {
		RegistroModificacion registroModificacion = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}

		if (idRegistroModificacion == null) {
			SolicitudGenerica solicitud = PresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
			idRegistroModificacion=solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
			if (idRegistroModificacion == null) {
				if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idRegistroModificacion"))
					Messages.fatal("Falta parámetro idRegistroModificacion");
			}
		}
		if (idSolicitud != null && idRegistroModificacion != null) {
			registroModificacion = RegistroModificacion.find("select registroModificacion from SolicitudGenerica solicitud join solicitud.registroModificacion registroModificacion where solicitud.id=? and registroModificacion.id=?", idSolicitud, idRegistroModificacion).first();
			if (registroModificacion == null)
				Messages.fatal("Error al recuperar RegistroModificacion");
		}
		return registroModificacion;
	}
	
	public static void index(String accion, Long idSolicitud, Long idRegistroModificacion, Long idRegistro) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/PresentarModificacionFAP/PresentarModificacionFAP.html");
		}

		SolicitudGenerica solicitud = PresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = PresentarModificacionFAPController.getRegistroModificacion(idSolicitud, idRegistroModificacion);

		Registro registro = null;
		if ("crear".equals(accion)) {
			registro = PresentarModificacionFAPController.getRegistro();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				registro.save();
				idRegistro = registro.id;
				registroModificacion.registro = registro;
				registroModificacion.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			registro = PresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);

		log.info("Visitando página: " + "gen/PresentarModificacionFAP/PresentarModificacionFAP.html");
		renderTemplate("gen/PresentarModificacionFAP/PresentarModificacionFAP.html", accion, idSolicitud, idRegistroModificacion, idRegistro, solicitud, registroModificacion, registro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void deshacer(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String botonModificar) {
		checkAuthenticity();
		if (!permisoDeshacer("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
			idRegistroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
			//solicitud.estado = "borrador"; TODO: ¿Estado?
			try {
				TramiteBase tramite = PresentacionModificacionFapController.invoke("getTramiteObject", idSolicitud);
				tramite.deshacer();
			} catch (Throwable e) {
				log.info("No se ha podido deshacer la presentación de la solicitud: "+e.getMessage());
			}
		}

		if (!Messages.hasErrors()) {
			PresentarModificacionFAPController.deshacerValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PresentarModificacionFAP/PresentarModificacionFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PresentarModificacionFAP/PresentarModificacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PresentarModificacionFAPController.deshacerRender(idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void prepararFirmar(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String botonPrepararFirmar) {
		checkAuthenticity();
		if (!permisoPrepararFirmar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionModificacionFapController.invoke("getTramiteObject", idSolicitud);
				if (PresentacionModificacionFapController.invoke("comprobarPaginasGuardadas", idSolicitud)){
					// Valido si hay alguna pagina sin guardar y si da error
					// No evaluo los documentos;
					if (!Messages.hasErrors()) {
						tramite.validarReglasConMensajes();
					}
	
					// Si no da fallos => genero el documento
					if (!Messages.hasErrors()) {
						tramite.prepararFirmar();
					}
				}
			} catch (Throwable e) {
				play.Logger.error("Hubo un problema al intentar invocar a los métodos de la clase PresentacionModificacionFAPController en prepararFirmar: "+e.getMessage());
				Messages.error("No se pudo preparar para Firmar");
			}
		}

		if (!Messages.hasErrors()) {
			PresentarModificacionFAPController.prepararFirmarValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PresentarModificacionFAP/PresentarModificacionFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PresentarModificacionFAP/PresentarModificacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		PresentarModificacionFAPController.prepararFirmarRender(idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	@Util
	public static void frmPresentarRender(Long idSolicitud, Long idRegistroModificacion, Long idRegistro) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("SolicitudPresentarModificacionFAPController.index", "editar", idSolicitud, idRegistroModificacion, idRegistro);
		}
		Messages.keep();
		redirect("PresentarModificacionFAPController.index", "editar", idSolicitud, idRegistroModificacion, idRegistro);
	}
	
}
