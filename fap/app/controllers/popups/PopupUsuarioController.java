
package controllers.popups;

import messages.Messages;
import models.Agente;
import play.Logger;
import controllers.gen.popups.PopupUsuarioControllerGen;
			
public class PopupUsuarioController extends PopupUsuarioControllerGen {
	
	public static void editar(Long idAgente,Agente agente){
        checkAuthenticity();
        if(!permiso("editar")){
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }
        
        Agente dbagente = null;
        if(!Messages.hasErrors()){
        	dbagente = getAgente(idAgente);
        }

        if(!Messages.hasErrors()){
            PopupUsuarioValidateCopy("editar", dbagente, agente);
        }
        
        if(!Messages.hasErrors()){
        	if (!dbagente.roles.contains(dbagente.rolActivo)) {
            	dbagente.rolActivo = dbagente.roles.iterator().next();
        		Logger.info("Popup: cambiando rol a " + dbagente.rolActivo);
        	}
        }

        if(!Messages.hasErrors()){
            dbagente.save();
        }

        if(!Messages.hasErrors()){
            renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
        }else{
            Messages.keep();
            index("editar",idAgente);
        }

    }
	
	public static void crear(Agente agente){
        checkAuthenticity();
        if(!permiso("crear")){
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }

        Agente dbagente = new Agente();
        

        if(!Messages.hasErrors()){
            PopupUsuarioValidateCopy("crear", dbagente, agente);
        }

        if(!Messages.hasErrors()){
        	if (!dbagente.roles.contains(dbagente.rolActivo)) {
            	dbagente.rolActivo = dbagente.roles.iterator().next();
        		Logger.info("Popup: cambiando rol a " + dbagente.rolActivo);
        	}
        }
        
        if(!Messages.hasErrors()){
            dbagente.save();
        }

        if(!Messages.hasErrors()){
            renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
        }else{
            Messages.keep();
            index("crear",  null);
        }
    }
}
		