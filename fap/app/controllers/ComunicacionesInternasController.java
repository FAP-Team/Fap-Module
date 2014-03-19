package controllers;

import messages.Messages;
import models.Agente;
import models.ComunicacionInterna;
import models.SolicitudGenerica;
import play.mvc.Util;
import controllers.fap.AgenteController;
import controllers.gen.ComunicacionesInternasControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class ComunicacionesInternasController extends ComunicacionesInternasControllerGen {
//	@Util
//	public static void formBotonRender(Long idSolicitud) {
//		if (!Messages.hasMessages()) {
//			Messages.ok("Página editada correctamente");
//			Messages.keep();
//			ComunicacionInterna ci = new ComunicacionInterna();
//			SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
//			//Asocio la nueva comunicacion a la solicitud
//			solicitud.comunicacionesInternas.add(ci);
//			ci.estado = EstadosComunicacionInternaEnum.creada.name();
//			ci.save();
//			solicitud.save();
//			redirect("PaginaNuevaComunicacionDocumentosController.index", "editar", idSolicitud, ci.id);
//		}
//		Messages.keep();
//		redirect("ComunicacionesInternasController.index", "editar", idSolicitud);
//	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formBoton(Long idSolicitud, String botonNuevaComunicacion) {
		checkAuthenticity();
		if (!permisoFormBoton("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			ComunicacionInterna ci = new ComunicacionInterna();
			SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
			//Asocio la nueva comunicacion a la solicitud
			solicitud.comunicacionesInternas.add(ci);
			ci.estado = EstadosComunicacionInternaEnum.creada.name();
			solicitud.save();
			redirect("PaginaNuevaComunicacionDocumentosController.index", "editar", idSolicitud, ci.id);
		}
		Messages.keep();

		if (!Messages.hasErrors()) {
			ComunicacionesInternasController.formBotonValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/ComunicacionesInternas/ComunicacionesInternas.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/ComunicacionesInternas/ComunicacionesInternas.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		ComunicacionesInternasController.formBotonRender(idSolicitud);
	}
	
	
}
