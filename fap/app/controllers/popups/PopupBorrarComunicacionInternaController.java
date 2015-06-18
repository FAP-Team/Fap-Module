package controllers.popups;

import com.google.inject.spi.Message;

import play.mvc.Util;
import messages.Messages;
import models.Agente;
import models.ComunicacionInterna;
import models.SolicitudGenerica;
import controllers.fap.AgenteController;
import controllers.gen.popups.PopupBorrarComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PopupBorrarComunicacionInternaController extends PopupBorrarComunicacionInternaControllerGen {

	public static void borrar(Long idSolicitud, Long idComunicacionInterna) {
		checkAuthenticity();
		
		if (!permiso("borrar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		
		ComunicacionInterna dbComunicacionInterna = PopupBorrarComunicacionInternaController.getComunicacionInterna(idSolicitud, idComunicacionInterna);
		SolicitudGenerica dbSolicitud = PopupBorrarComunicacionInternaController.getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			PopupBorrarComunicacionInternaController.borrarValidateRules(dbComunicacionInterna);
		}

		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbSolicitud.comunicacionesInternas.remove(dbComunicacionInterna);
			dbSolicitud.save();

			dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.cancelada.name();
			dbComunicacionInterna.save();

			log.info("Acción Borrar de página: " + "gen/popups/PopupBorrarComunicacionInterna.html" + " , intentada con éxito" + " Agente: " + logAgente);
		} else {
			log.info("Acción Borrar de página: " + "gen/popups/PopupBorrarComunicacionInterna.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		}
		PopupBorrarComunicacionInternaController.borrarRender(idSolicitud, idComunicacionInterna);
	}
	
}
