package controllers;

import com.google.inject.spi.Message;

import messages.Messages;
import models.Agente;
import models.ComunicacionInterna;
import models.ReturnComunicacionInternaFap;
import play.mvc.Util;
import properties.FapProperties;
import services.ComunicacionesInternasService;
import services.ComunicacionesInternasServiceException;
import services.RegistroLibroResolucionesService;
import services.comunicacionesInternas.ComunicacionesInternasServiceImpl;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PaginaNuevoAsientoControllerGen;

public class PaginaNuevoAsientoController extends PaginaNuevoAsientoControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmNuevoAsiento(Long idSolicitud, Long idComunicacionInterna, ComunicacionInterna comunicacionInterna, String btnNuevoAsiento) {
		checkAuthenticity();
		if (!permisoFrmNuevoAsiento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		ComunicacionInterna dbComunicacionInterna = PaginaNuevoAsientoController.getComunicacionInterna(idSolicitud, idComunicacionInterna);
		PaginaNuevoAsientoController.frmNuevoAsientoBindReferences(comunicacionInterna);

		if (!Messages.hasErrors()) {
			PaginaNuevoAsientoController.frmNuevoAsientoValidateCopy("editar", dbComunicacionInterna, comunicacionInterna);
		}

		if (!Messages.hasErrors()) {
			PaginaNuevoAsientoController.frmNuevoAsientoValidateRules(dbComunicacionInterna, comunicacionInterna);
		}
		
		if (!Messages.hasErrors()) {
			String usuario = FapProperties.get("fap.platino.registro.username");
			String password = FapProperties.get("fap.platino.registro.password");
			ComunicacionesInternasService comunicacionesService = InjectorConfig.getInjector().getInstance(ComunicacionesInternasService.class);
			try {
				comunicacionesService.crearNuevoAsiento(dbComunicacionInterna.asiento);
			} catch (ComunicacionesInternasServiceException e) {
				e.printStackTrace();
			}
			
		}
		
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbComunicacionInterna.save();
			log.info("Acción Editar de página: " + "gen/PaginaNuevoAsiento/PaginaNuevoAsiento.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNuevoAsiento/PaginaNuevoAsiento.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		PaginaNuevoAsientoController.frmNuevoAsientoRender(idSolicitud, idComunicacionInterna);
	}
	
}
