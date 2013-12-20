package controllers;

import messages.Messages;
import models.Agente;
import play.mvc.Util;
import services.VerificarDatosServiceException;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.VerificacionDatosControllerGen;
import es.gobcan.platino.servicios.svd.Respuesta;
import services.VerificarDatosService;

public class VerificacionDatosController extends VerificacionDatosControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void verificacionNueva(Long idSolicitud, String botonIniciarVerificacionDatos) {
		checkAuthenticity();
		if (!permisoVerificacionNueva("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {

			String accion = getAccion();
			redirect("DatosVerificadosController.index", accion, idSolicitud);
		}

		if (!Messages.hasErrors()) {
			VerificacionDatosController.verificacionNuevaValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/VerificacionDatos/VerificacionDatos.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/VerificacionDatos/VerificacionDatos.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		VerificacionDatosController.verificacionNuevaRender(idSolicitud);
	}

}
