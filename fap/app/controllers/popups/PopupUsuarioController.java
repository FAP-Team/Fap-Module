
package controllers.popups;

import java.util.Map;

import messages.Messages;
import models.Agente;
import play.Logger;
import play.libs.Crypto;
import play.mvc.Util;
import utils.StringUtils;
import validation.CustomValidation;
import controllers.gen.popups.PopupUsuarioControllerGen;
			
public class PopupUsuarioController extends PopupUsuarioControllerGen {
	
	public static void index(String accion, Long idAgente, String urlRedirigir) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/popups/PopupUsuario.html");
		}

		Agente agente = null;
		if ("crear".equals(accion)) {
			agente = PopupUsuarioController.getAgente();
			String variablesRedirigir = "";
			urlRedirigir += variablesRedirigir;
		}
		else if (!"borrado".equals(accion))
			agente = PopupUsuarioController.getAgente(idAgente);

		log.info("Visitando página: " + "fap/Admin/PopupUsuario.html");
		renderTemplate("fap/Admin/PopupUsuario.html", accion, idAgente, agente, urlRedirigir);
	}
	
	public static void editar(Long idAgente, Agente agente){
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
            redirect("popups.PopupUsuarioController.index", "editar", idAgente);
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
            redirect("popups.PopupUsuarioController.index", "crear", agente.id);
        }
    }
	
	@Util
	public static void PopupUsuarioValidateCopy(String accion, Agente dbAgente, Agente agente) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("agente", agente);
		
		CustomValidation.required("agente.username", agente.username);
		dbAgente.username = agente.username;

		if (secure.checkGrafico("accesoNoCertificado", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			
			if (!agente.password.isEmpty()){
				if ((dbAgente.password == null) || (Crypto.passwordHash(agente.passwordAntiguo).equals(dbAgente.password))){
					CustomValidation.compare("agente.password", agente.password, params.get("agente_passwordcopy"));
					dbAgente.password = Crypto.passwordHash(agente.password);
				} else {
					CustomValidation.error("El password antiguo no es correcto", "agente.passwordAntiguo", agente.passwordAntiguo);
				}
			} else {
				if (!agente.passwordAntiguo.isEmpty()){
					CustomValidation.error("La longitud de la contraseña debe tener entre 6 y 10 carateres", "agente.password", agente.password);
				}
			}
		}
		
		CustomValidation.required("agente.roles", agente.roles);
		CustomValidation.validListOfValuesFromTable("agente.roles", agente.roles);

		dbAgente.roles.retainAll(agente.roles);
		dbAgente.roles.addAll(agente.roles);
		CustomValidation.required("agente.email", agente.email);
		dbAgente.email = agente.email;
        dbAgente.cargo = agente.cargo;
        dbAgente.usuarioldap = agente.usuarioldap;
		dbAgente.funcionario = agente.funcionario;
		
	}
	
}
		