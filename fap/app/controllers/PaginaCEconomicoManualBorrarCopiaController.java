package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import utils.PeticionModificacion;
import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import models.JsonPeticionModificacion;
import models.SolicitudGenerica;
import controllers.gen.PaginaCEconomicoManualBorrarCopiaControllerGen;
import controllers.popups.PopUpCEconomicoManualBorrarController;

public class PaginaCEconomicoManualBorrarCopiaController extends PaginaCEconomicoManualBorrarCopiaControllerGen {
	public static void borrar(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		CEconomicosManuales dbCEconomicosManuales = PaginaCEconomicoManualBorrarCopiaController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);
		CEconomico dbCEconomico = PaginaCEconomicoManualBorrarCopiaController.getCEconomico(idSolicitud, idCEconomico);
		if (!Messages.hasErrors()) {
			PaginaCEconomicoManualBorrarCopiaController.borrarValidateRules(dbCEconomicosManuales);
		}
		if (!Messages.hasErrors()) {
		
			//----- Modificacion de Solicitudes -------------//
			
			PeticionModificacion peticionModificacion = new PeticionModificacion();
			peticionModificacion.campoPagina = "Solicitud.ceconomicos.otros"; //OJO
			SolicitudGenerica dbSolicitud = PaginaCEconomicoManualController.getSolicitudGenerica(idSolicitud);

			Map<String, String> allSimple = params.allSimple();
			for (Map.Entry<String, String> entry : allSimple.entrySet()) {
				if (entry.getKey().startsWith("id")) {
					try {
						peticionModificacion.idSimples.put(entry.getKey(), Long.parseLong(entry.getValue()));
					} catch (Exception e) {
						//El parámetro no era un long
					}
				}
			}
			List<String> valoresAntiguos = new ArrayList<String>();
			peticionModificacion.idSimples.put("idCEconomicosManuales",idCEconomicosManuales); //ID que "borro" 
			valoresAntiguos.add(dbCEconomico.tipo.nombre.toString());
			peticionModificacion.setValorBorrado("Solicitud.ceconomicos.otros", new ArrayList<String>(), valoresAntiguos); //<-
			
			Gson gson = new Gson();
			String jsonPM = gson.toJson(peticionModificacion);
			JsonPeticionModificacion jsonPeticionModificacion = new JsonPeticionModificacion();
			jsonPeticionModificacion.jsonPeticion = jsonPM;
			dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).jsonPeticionesModificacion.add(jsonPeticionModificacion);
			dbSolicitud.save();

			dbCEconomico.otros.remove(dbCEconomicosManuales);
			System.out.println("BORRADO: "+dbCEconomicosManuales.id);
			for (CEconomicosManuales id : dbCEconomico.otros) {
				System.out.println("QUEDA: "+id.id);
			}
			dbCEconomico.save();	
			log.info("Acción Borrar de página: " + "gen/popups/PaginaCEconomicoManualBorrar.html" + " , intentada con éxito");
		} else {
			log.info("Acción Borrar de página: " + "gen/popups/PaginaCEconomicoManualBorrar.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PaginaCEconomicoManualBorrarCopiaController.borrarRender(idSolicitud, idCEconomico, idCEconomicosManuales);
	}
	
}
