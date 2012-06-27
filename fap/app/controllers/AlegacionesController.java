package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import play.mvc.Util;
import tramitacion.TramiteAlegacion;
import controllers.gen.AlegacionesControllerGen;

public class AlegacionesController extends AlegacionesControllerGen {
	
	@Util
	public static void prepararFirmar(Long idSolicitud, String botonPrepararFirmar) {
		checkAuthenticity();
		if (!permisoPrepararFirmar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AlegacionesController.getSolicitudGenerica(idSolicitud);
		TramiteAlegacion trAlegacion = new TramiteAlegacion(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAlegacion.prepararFirmar();
		}

		if (!Messages.hasErrors()) {
			AlegacionesController.prepararFirmarValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/Alegaciones/Alegaciones.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/Alegaciones/Alegaciones.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AlegacionesController.prepararFirmarRender(idSolicitud);
	}
	
	@Util
	public static void deshacer(Long idSolicitud, String botonModificar) {
		checkAuthenticity();
		if (!permisoDeshacer("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = AlegacionesController.getSolicitudGenerica(idSolicitud);
		TramiteAlegacion trAlegacion = new TramiteAlegacion(dbSolicitud);
		
		if (!Messages.hasErrors()) {
			trAlegacion.deshacer();
		}

		if (!Messages.hasErrors()) {
			AlegacionesController.deshacerValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/Alegaciones/Alegaciones.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/Alegaciones/Alegaciones.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AlegacionesController.deshacerRender(idSolicitud);
	}
	
	
	
}
