package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import play.mvc.Util;
import tramitacion.TramiteBase;
import controllers.fap.PresentacionFapController;
import controllers.fap.VerificacionFapController;
import controllers.gen.PresentarFAPControllerGen;
import enumerado.fap.gen.EstadosModificacionEnum;

public class PresentarFAPController extends PresentarFAPControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void deshacer(Long idSolicitud, Long idRegistro, String botonModificar) {
		checkAuthenticity();
		if (!permisoDeshacer("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
			solicitud.estado = "borrador";
			try {
				TramiteBase tramite = PresentacionFapController.invoke(PresentacionFapController.class, "getTramiteObject", idSolicitud);
				tramite.deshacer();
			} catch (Throwable e) {
				log.info("No se ha podido deshacer la presentación de la solicitud: "+e.getMessage());
			}
			
		}

		if (!Messages.hasErrors()) {
			PresentarFAPController.deshacerValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PresentarFAP/PresentarFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PresentarFAP/PresentarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		PresentarFAPController.deshacerRender(idSolicitud, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void prepararFirmar(Long idSolicitud, Long idRegistro, String botonPrepararFirmar) {
		checkAuthenticity();
		if (!permisoPrepararFirmar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			
			try {
				TramiteBase tramite = PresentacionFapController.invoke(PresentacionFapController.class, "getTramiteObject", idSolicitud);
				if (PresentacionFapController.invoke(PresentacionFapController.class, "comprobarPaginasGuardadas", idSolicitud)){
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
				log.error("Hubo un problema al intentar invocar a los métodos de la clase PresentacionFAPController en prepararFirmar: "+e.getMessage());
				Messages.error("No se pudo preparar para Firmar");
			}
		}

		if (!Messages.hasErrors()) {
			PresentarFAPController.prepararFirmarValidateRules();
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PresentarFAP/PresentarFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PresentarFAP/PresentarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		PresentarFAPController.prepararFirmarRender(idSolicitud, idRegistro);
	}
	
	@Util
	public static void frmPresentarRender(Long idSolicitud, Long idRegistro) {
		if (!Messages.hasMessages()) {
			Messages.keep();
			redirect("SolicitudPresentarFAPController.index", "editar", idSolicitud, idRegistro);
		}
		Messages.keep();
		redirect("PresentarFAPController.index", "editar", idSolicitud, idRegistro);
	}

	
	public static void tablatablaModificaciones(Long idSolicitud) {

		java.util.List<RegistroModificacion> rows = RegistroModificacion.find("select registroModificacion from SolicitudGenerica solicitud join solicitud.registroModificacion registroModificacion where solicitud.id=?", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<RegistroModificacion> rowsFiltered = new ArrayList<RegistroModificacion>();
		for (RegistroModificacion registroModificacion : rows) {
			if (registroModificacion.estado.equals(EstadosModificacionEnum.registrada.name())){
				rowsFiltered.add(registroModificacion);
			}
		}

		tables.TableRenderResponse<RegistroModificacion> response = new tables.TableRenderResponse<RegistroModificacion>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("fechaCreacion", "fechaLimite", "fechaRegistro", "estado", "registro.justificante.urlDescarga", "id"));
	}
	
}
