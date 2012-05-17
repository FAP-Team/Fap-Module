package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import controllers.gen.ExclusionControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;

public class ExclusionController extends ExclusionControllerGen {

	public static void finalizarExclusion(Long idSolicitud, String finalizarExclusionBoton) {
		checkAuthenticity();
		if (!permisoFinalizarExclusion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica sol = getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			if (sol.exclusion.codigos.size() == 0) {
				Messages.error("Debe asignar al menos un códio de exclusión a la Solicitud para excluirla");
			}
		}

		if (!Messages.hasErrors()) {
			ExclusionController.finalizarExclusionValidateRules();
		}
		if (!Messages.hasErrors()) {
			Messages.ok("La Solicitud ha pasado al estado Excluido");
			sol.estado = EstadosSolicitudEnum.excluido.name();
			sol.save();
			log.info("Acción Editar de página: " + "gen/Exclusion/Exclusion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/Exclusion/Exclusion.html" + " , intentada sin éxito (Problemas de Validación)");
		ExclusionController.finalizarExclusionRender(idSolicitud);
	}
	
}
