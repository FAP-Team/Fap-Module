
package controllers.popups;

import messages.Messages;
import models.Agente;
import play.Logger;
import controllers.gen.popups.ListaUsuariosPopupControllerGen;
			
public class ListaUsuariosPopupController extends ListaUsuariosPopupControllerGen {

	public static void editar(Long idAgente,Agente agente){
        checkAuthenticity();
        if(!permiso("update")){
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }
        
        Agente dbagente = null;
        if(!Messages.hasErrors()){
        	dbagente = getAgente(idAgente);
        }

        if(!Messages.hasErrors()){
            PopupUsuarioValidateCopy(dbagente, agente);;
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
            abrir("editar",idAgente);
        }

    }
	
	public static void crear(Agente agente){
        checkAuthenticity();
        if(!permiso("create")){
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }

        Agente dbagente = new Agente();
        

        if(!Messages.hasErrors()){
            PopupUsuarioValidateCopy(dbagente, agente);;
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
            abrir("crear",null);
        }
    }
	
}
		