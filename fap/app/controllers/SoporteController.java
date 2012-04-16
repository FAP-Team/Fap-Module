
package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

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
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
		dbincidencia.fecha = df.format(new Date());

		if(!validation.hasErrors()){
			try {
				emails.Mails.enviar("incidencia",incidencia);
				dbincidencia.enviada=true;
			} catch (Exception e){
				dbincidencia.enviada=false;
				play.Logger.error("Error al enviar el correo con la incidencia "+incidencia.id+": "+e);
			}
			dbincidencia.save(); 
			Logger.info("Guardando incidencia");
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
