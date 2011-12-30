
package controllers;

import messages.Messages;
import models.Agente;
import models.Incidencia;
import play.Logger;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.mvc.Util;
import controllers.fap.AgenteController;
import controllers.fap.SecureController;
import controllers.gen.SoporteControllerGen;

public class SoporteController extends SoporteControllerGen {

	public static void crear(Incidencia incidencia){
		checkAuthenticity();
		if(!permiso("create")){
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Incidencia dbIncidencia = null;
		if(!Messages.hasErrors()){
			dbIncidencia = getIncidencia();
		}
		if(!Messages.hasErrors()){
	
		}

		if(!Messages.hasErrors()){
			SoporteValidateCopy(dbIncidencia, incidencia);
		}
		Long idIncidencia;
		if(!Messages.hasErrors()){
			Agente agente = AgenteController.getAgente();
			dbIncidencia.email = agente.email;
			dbIncidencia.nombre = agente.name;
			emails.Mails.enviar("incidencia",incidencia);
			dbIncidencia.save();
			
		}
		idIncidencia = dbIncidencia.id;
		crearRender(idIncidencia);
	}
	
}
