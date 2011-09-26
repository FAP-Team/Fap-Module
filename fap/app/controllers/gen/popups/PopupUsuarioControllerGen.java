
package controllers.gen.popups;

import play.*;
import play.mvc.*;
import play.db.jpa.Model;
import controllers.fap.*;
import validation.*;
import messages.Messages;

import models.*;
import tags.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;



public class PopupUsuarioControllerGen extends GenericController {

    
				@Util
				protected static Agente getAgente(Long idAgente){
					Agente agente = null;
					if(idAgente == null){
						Messages.fatal("Falta parámetro idAgente");
					}else{
						agente = Agente.findById(idAgente);
						if(agente == null){
							Messages.fatal("Error al recuperar Agente");
						}
					}
					return agente;
				}
			

    
	public static void abrir(String accion,Long idAgente){
		Agente agente;
		if(accion.equals("crear")){
            agente = new Agente();
		}else{
		    agente = getAgente(idAgente);
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "PopupUsuarioControllerGen");
		renderTemplate("gen/popups/PopupUsuario.html",accion,idAgente,agente);
	}
        

    
			@Util
            protected static boolean permiso(String accion) {
                //Sobreescribir para incorporar permisos a mano
			return true;
            }
        

    
                public static void crear(Agente agente){
                    checkAuthenticity();
                    if(!permiso("create")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    Agente dbAgente = new Agente();
                    

                    if(!Messages.hasErrors()){
                        PopupUsuarioValidateCopy(dbAgente, agente);;
                    }


                    if(!Messages.hasErrors()){
                        dbAgente.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("crear",null);
                    }
                }
            

    
                public static void editar(Long idAgente,Agente agente){
                    checkAuthenticity();
                    if(!permiso("update")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            Agente dbAgente = null;
            if(!Messages.hasErrors()){
                dbAgente = getAgente(idAgente);
            }
            

                    if(!Messages.hasErrors()){
                        PopupUsuarioValidateCopy(dbAgente, agente);;
                    }

                    if(!Messages.hasErrors()){
                        dbAgente.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("editar",idAgente);
                    }

                }
            

    
                public static void borrar(Long idAgente){
                    checkAuthenticity();
                    if(!permiso("delete")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            Agente dbAgente = null;
            if(!Messages.hasErrors()){
                dbAgente = getAgente(idAgente);
            }
            

                    if(!Messages.hasErrors()){
                        dbAgente.delete();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro borrado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("borrar",idAgente);
                    }
                }
            

    
			@Util
			protected static void PopupUsuarioValidateCopy(Agente dbAgente, Agente agente){
				CustomValidation.clearValidadas();
				CustomValidation.valid("agente", agente);
CustomValidation.required("agente.username", agente.username);
dbAgente.username = agente.username;
CustomValidation.validListOfValuesFromTable("agente.roles", agente.roles);
CustomValidation.required("agente.roles", agente.roles);

			dbAgente.roles.retainAll(agente.roles);
			dbAgente.roles.addAll(agente.roles);
			CustomValidation.required("agente.email", agente.email);
dbAgente.email = agente.email;

				
			}
		

    
}
