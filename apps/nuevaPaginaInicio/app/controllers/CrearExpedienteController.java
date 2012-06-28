package controllers;

import messages.Messages;
import controllers.gen.CrearExpedienteControllerGen;
import models.ExpedienteGenerico;

public class CrearExpedienteController extends CrearExpedienteControllerGen {

	public static void index(String accion, Long idSolicitud){
		if (permiso("editar")) {
			if(!validation.hasErrors()){
				try {
					ExpedienteGenerico exp = (ExpedienteGenerico) InitControllerNuevaPaginaInicio.inicialize();
					Messages.keep();
					redirect("CrearExpedientesController.index", "editar", exp.id);
				} catch (Throwable e) {
					System.out.println("Error al realizar la llamada al iniciar la nueva solicitud.");
					e.printStackTrace();
				}
			}
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		SolicitudesController.index("editar");
	}
	
	
}
