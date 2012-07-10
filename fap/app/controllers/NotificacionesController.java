package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Agente;
import models.Interesado;
import models.Notificacion;
import controllers.fap.AgenteController;
import controllers.gen.NotificacionesControllerGen;

public class NotificacionesController extends NotificacionesControllerGen {

	public static void tablalistaNotificaciones() {

		java.util.List<Notificacion> rows = Notificacion.find("select notificacion from Notificacion notificacion").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Notificacion> rowsFiltered = new ArrayList<Notificacion>(); 
		
		Agente agente = AgenteController.getAgente();
		
		if (agente != null){
			for (Notificacion notificacion: rows){
				for (Interesado interesado: notificacion.interesados){
					if ((interesado.persona.getNumeroId() != null) && (interesado.persona.getNumeroId().equals(agente.username))){
						rowsFiltered.add(notificacion);
						break;
					}
				}
			}
		}
		
		tables.TableRenderResponse<Notificacion> response = new tables.TableRenderResponse<Notificacion>(rowsFiltered, true, false, false, "notificacionEditableSiNoLeida", "", "", getAccion(), ids);

		renderJSON(response.toJSON("asunto", "descripcion", "fechaPuestaADisposicion", "estado", "id"));
	}
	
}
