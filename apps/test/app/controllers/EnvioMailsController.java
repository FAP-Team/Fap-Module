
			package controllers;

			import java.util.HashMap;
import java.util.Map;

import messages.Messages;
import controllers.gen.EnvioMailsControllerGen;
import emails.Mails;
			
			public class EnvioMailsController extends EnvioMailsControllerGen {

				public static void enviarMail(Long idSolicitud){
					checkAuthenticity();

					

					// Save code
					if (permisoenviarMail("update") || permisoenviarMail("create")) {
					
						
					
						

						if(!validation.hasErrors()){
							
						}
						
						if(!validation.hasErrors()){
							
					
				
						}
					}
					else {
						Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					}

				
					if (!Messages.hasErrors()){
						Map<String, Object> argsVacios = new HashMap<String, Object>();
						Mails.enviar("prueba", argsVacios);
					}
					enviarMailRender(idSolicitud);

				}
				
			}
		