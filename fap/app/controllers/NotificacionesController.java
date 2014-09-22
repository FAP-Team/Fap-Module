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
import enumerado.fap.gen.EstadoNotificacionEnum;

public class NotificacionesController extends NotificacionesControllerGen {

	public static void tablalistaNotificaciones() {

		java.util.List<Notificacion> rows = Notificacion.find("select notificacion from Notificacion notificacion").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Notificacion> rowsFiltered = new ArrayList<Notificacion>(); 
		
		Agente agente = AgenteController.getAgente();
		
		if (agente != null){
			for (Notificacion notificacion: rows){
				for (Interesado interesado: notificacion.interesados){
					if (((interesado.persona.getNumeroId() != null) && (interesado.persona.getNumeroId().equals(agente.username))) || ((agente.rolActivo.equals("gestor") || agente.rolActivo.equals("gestorTenerife") || agente.rolActivo.equals("gestorLasPalmas") || agente.rolActivo.equals("administrador") || agente.rolActivo.equals("revisor"))) 
							&& (notificacion.fechaPuestaADisposicion != null) && (!notificacion.estado.equals(EstadoNotificacionEnum.creada.name())) && (notificacion.uri!=null)){
						rowsFiltered.add(notificacion);
						break;
					}
				}
			}
		}
		tables.TableRenderResponse<Notificacion> response = new tables.TableRenderResponse<Notificacion>(rowsFiltered, true, false, false, "notificacionEditableSiNoLeida", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idExpedienteAed", "fechaPuestaADisposicion", "fechaLimite", "fechaFinPlazo", "uri", "estado", "id"));
	}
	
}
