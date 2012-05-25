
package controllers.popups;

import messages.Messages;
import models.Agente;
import play.Logger;
import play.libs.Crypto;
import play.mvc.Util;
import validation.CustomValidation;
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
	
	@Util
	public static void PopupUsuarioValidateCopy(String accion, Agente dbAgente, Agente agente) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("agente", agente);
		CustomValidation.required("agente.username", agente.username);
		dbAgente.username = agente.username;
		CustomValidation.required("agente.password", agente.password);
		CustomValidation.compare(agente.password, params.get("agente_passwordcopy"));
		//dbAgente.password = agente.password;
		dbAgente.password = Crypto.passwordHash(agente.password);
		CustomValidation.required("agente.roles", agente.roles);
		CustomValidation.validListOfValuesFromTable("agente.roles", agente.roles);

		dbAgente.roles.retainAll(agente.roles);
		dbAgente.roles.addAll(agente.roles);
		CustomValidation.required("agente.email", agente.email);
		dbAgente.email = agente.email;
		dbAgente.funcionario = agente.funcionario;

	}
	
}
		