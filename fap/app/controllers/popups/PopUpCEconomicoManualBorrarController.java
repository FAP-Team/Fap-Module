package controllers.popups;

import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import controllers.gen.popups.PopUpCEconomicoManualBorrarControllerGen;

public class PopUpCEconomicoManualBorrarController extends PopUpCEconomicoManualBorrarControllerGen {

	public static void borrar(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		CEconomicosManuales dbCEconomicosManuales = PopUpCEconomicoManualBorrarController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);
		CEconomico dbCEconomico = PopUpCEconomicoManualBorrarController.getCEconomico(idSolicitud, idCEconomico);
		if (!Messages.hasErrors()) {
			PopUpCEconomicoManualBorrarController.borrarValidateRules(dbCEconomicosManuales);
		}
		if (!Messages.hasErrors()) {
			dbCEconomicosManuales.tipo=null;
			dbCEconomico.otros.remove(dbCEconomicosManuales);
			dbCEconomico.save();

			dbCEconomicosManuales.valores.clear();

			dbCEconomicosManuales.delete();

			log.info("Acción Borrar de página: " + "gen/popups/PopUpCEconomicoManualBorrar.html" + " , intentada con éxito");
		} else {
			log.info("Acción Borrar de página: " + "gen/popups/PopUpCEconomicoManualBorrar.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PopUpCEconomicoManualBorrarController.borrarRender(idSolicitud, idCEconomico, idCEconomicosManuales);
	}
	
}
