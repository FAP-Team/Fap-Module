
package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import messages.Messages;
import models.Agente;
import models.Incidencia;
import play.Logger;
import play.mvc.Router;
import play.mvc.Scope;
import play.mvc.Router.Route;
import play.mvc.Scope.Session;
import play.mvc.Util;
import controllers.fap.AgenteController;
import controllers.fap.SecureController;
import controllers.gen.SoporteControllerGen;

public class SoporteController extends SoporteControllerGen {
	
	public static void crear(Incidencia incidencia){
		checkAuthenticity();
		if(!permiso("crear")){
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Incidencia dbIncidencia = null;
		if(!Messages.hasErrors()){
			dbIncidencia = getIncidencia();
		}
		if(!Messages.hasErrors()){
			SoporteValidateCopy("crear", dbIncidencia, incidencia);
		}
		Long idIncidencia;
		if(!Messages.hasErrors()){
			Agente agente = AgenteController.getAgente();
			if (agente != null){
				dbIncidencia.email = agente.email;
				dbIncidencia.nombre = agente.name;
			}
			DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
			dbIncidencia.fecha= df.format(new Date());
			try {
				emails.Mails.enviar("incidencia",incidencia);
				dbIncidencia.enviada=true;
			} catch (Exception e){
				dbIncidencia.enviada=false;
				play.Logger.error("Error al enviar el correo con la incidencia "+incidencia.id+": "+e);
			}
			dbIncidencia.save();
			
		}
		idIncidencia = dbIncidencia.id;
		crearRender(idIncidencia);
	}
	
}
