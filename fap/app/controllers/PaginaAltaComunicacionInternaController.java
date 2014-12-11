package controllers;

import javax.inject.Inject;

import messages.Messages;
import models.Agente;
import models.ComunicacionInterna;
import models.ReturnComunicacionInternaFap;
import play.mvc.Util;
import services.ComunicacionesInternasServiceException;
import services.comunicacionesInternas.ComunicacionesInternasServiceImpl;
import validation.CustomValidation;
import controllers.fap.AgenteController;
import controllers.gen.PaginaAltaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PaginaAltaComunicacionInternaController extends PaginaAltaComunicacionInternaControllerGen {
	
	@Inject
	protected static ComunicacionesInternasServiceImpl ciService;
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmNuevoAsiento(Long idSolicitud, Long idComunicacionInterna, ComunicacionInterna comunicacionInterna, String btnNuevoAsiento) {
		checkAuthenticity();
		if (!permisoFrmNuevoAsiento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ComunicacionInterna dbComunicacionInterna = PaginaAltaComunicacionInternaController.getComunicacionInterna(idSolicitud, idComunicacionInterna);

		PaginaAltaComunicacionInternaController.frmNuevoAsientoBindReferences(comunicacionInterna);

		if (!Messages.hasErrors()) {
			PaginaAltaComunicacionInternaController.frmNuevoAsientoValidateCopy("editar", dbComunicacionInterna, comunicacionInterna);
		}

		if (!Messages.hasErrors()) {
			PaginaAltaComunicacionInternaController.frmNuevoAsientoValidateRules(dbComunicacionInterna, comunicacionInterna);
		}
		
		if (!Messages.hasErrors()) {
			try {
				ReturnComunicacionInternaFap respuesta = ciService.crearNuevoAsiento(dbComunicacionInterna.asiento);
				
				if (respuesta.error.descripcion == null) {
					dbComunicacionInterna.respuesta = respuesta;
					dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.enviada.name();
				}
			} catch (ComunicacionesInternasServiceException e) {
				log.error("No se ha podido crear el alta de la comunicación interna: "+e.getMessage());
				Messages.error("No se ha podido crear el alta de la comunicación interna");
			}
		}
		
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbComunicacionInterna.save();
			log.info("Acción Editar de página: " + "gen/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		PaginaAltaComunicacionInternaController.frmNuevoAsientoRender(idSolicitud, idComunicacionInterna);
	}
	
	@Util
	public static void frmNuevoAsientoRender(Long idSolicitud, Long idComunicacionInterna) {
		if (!Messages.hasErrors()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("ComunicacionesInternasController.index", ComunicacionesInternasController.getAccion(), idSolicitud, idComunicacionInterna);
		}
		Messages.keep();
		redirect("PaginaAltaComunicacionInternaController.index", "editar", idSolicitud, idComunicacionInterna);
	}
	
}
