
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



public class PopupEmailControllerGen extends GenericController {

    
				@Util
				protected static Mail getMail(Long idMail){
					Mail mail = null;
					if(idMail == null){
						Messages.fatal("Falta parámetro idMail");
					}else{
						mail = Mail.findById(idMail);
						if(mail == null){
							Messages.fatal("Error al recuperar Mail");
						}
					}
					return mail;
				}
			

    
	public static void abrir(String accion,Long idMail){
		Mail mail;
		if(accion.equals("crear")){
            mail = new Mail();
		}else{
		    mail = getMail(idMail);
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "PopupEmailControllerGen");
		renderTemplate("gen/popups/PopupEmail.html",accion,idMail,mail);
	}
        

    
			@Util
            protected static boolean permiso(String accion) {
                //Sobreescribir para incorporar permisos a mano
			return true;
            }
        

    
                public static void crear(Mail mail){
                    checkAuthenticity();
                    if(!permiso("create")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    Mail dbMail = new Mail();
                    

                    if(!Messages.hasErrors()){
                        PopupEmailValidateCopy(dbMail, mail);;
                    }


                    if(!Messages.hasErrors()){
                        dbMail.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("crear",null);
                    }
                }
            

    
                public static void editar(Long idMail,Mail mail){
                    checkAuthenticity();
                    if(!permiso("update")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            Mail dbMail = null;
            if(!Messages.hasErrors()){
                dbMail = getMail(idMail);
            }
            

                    if(!Messages.hasErrors()){
                        PopupEmailValidateCopy(dbMail, mail);;
                    }

                    if(!Messages.hasErrors()){
                        dbMail.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("editar",idMail);
                    }

                }
            

    
                public static void borrar(Long idMail){
                    checkAuthenticity();
                    if(!permiso("delete")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            Mail dbMail = null;
            if(!Messages.hasErrors()){
                dbMail = getMail(idMail);
            }
            

                    if(!Messages.hasErrors()){
                        dbMail.delete();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro borrado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("borrar",idMail);
                    }
                }
            

    
			@Util
			protected static void PopupEmailValidateCopy(Mail dbMail, Mail mail){
				CustomValidation.clearValidadas();
				CustomValidation.valid("mail", mail);
CustomValidation.required("mail.idMail", mail.idMail);
dbMail.idMail = mail.idMail;
dbMail.sender = mail.sender;
CustomValidation.required("mail.sendTo", mail.sendTo);
dbMail.sendTo = mail.sendTo;
dbMail.bcc = mail.bcc;
CustomValidation.required("mail.subject", mail.subject);
dbMail.subject = mail.subject;
CustomValidation.required("mail.content", mail.content);
dbMail.content = mail.content;
dbMail.footer = mail.footer;

				
			}
		

    
}
