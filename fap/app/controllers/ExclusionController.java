
package controllers;

import java.util.List;

import messages.Messages;
import models.Exclusion;
import models.SolicitudGenerica;
import models.TipoCodigoExclusion;
import controllers.gen.ExclusionControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;

public class ExclusionController extends ExclusionControllerGen {
	public static void finalizarExclusion(Long idSolicitud) {
		checkAuthenticity();

		// Save code
		if (permisofinalizarExclusion("update")
				|| permisofinalizarExclusion("create")) {
			SolicitudGenerica sol = getSolicitudGenerica(idSolicitud);
			
			if (!validation.hasErrors()) {
				if (sol.exclusion.codigos.size() == 0) {
					Messages.error("Debe asignar al menos un códio de exclusión a la Solicitud para excluirla");
				}
			}

			if (!validation.hasErrors()) {
				Messages.ok("La Solicitud ha pasado al estado Excluido");
				sol.estado = EstadosSolicitudEnum.excluido.name();
				sol.save();
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		finalizarExclusionRender(idSolicitud);

	}
}
