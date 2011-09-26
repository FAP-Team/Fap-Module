
package controllers;

import messages.Messages;
import models.Agente;
import models.Incidencia;
import play.Logger;
import play.mvc.Util;
import controllers.fap.AgenteController;
import controllers.fap.SecureController;
import controllers.gen.SoporteControllerGen;

public class SoporteController extends SoporteControllerGen {

	public static void index(){
		Incidencia incidencia = getIncidencia();
		Agente agente = AgenteController.getAgente();
		if(agente != null){
			//Rellena campos de la incidencia con la información del agente
			incidencia.email = agente.email;
			incidencia.nombre = agente.name;
		}
		
		
		renderTemplate( "gen/Soporte/Soporte.html" , incidencia);
	}
	
	public static void incidencia(Incidencia incidencia){
		checkAuthenticity();
		
		Incidencia dbincidencia = getIncidencia();

		incidenciaValidateCopy(dbincidencia, incidencia);

		if(!validation.hasErrors()){
			emails.Mails.enviar("incidencia",incidencia);
			dbincidencia.save(); Logger.info("Guardando incidencia");
		}
		incidenciaRender();
	}
	
	@Util
	public static void incidenciaRender(){
		
		if (!Messages.hasMessages()) {
			Messages.ok("Su consulta o incidencia se ha enviado correctamente");
		}		
	
		Messages.keep();
		redirect( "SoporteController.index" );
	}

}
