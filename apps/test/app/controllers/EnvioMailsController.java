
			package controllers;

			import java.util.HashMap;
import java.util.Map;

import play.Logger;

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
						try{
							Mails.enviar("prueba", argsVacios);
						} catch (IllegalArgumentException e){
							Logger.error("No se encontró el ID del mail en la base de datos");
						} catch (Exception e){
							Logger.error("Problemas con la plantilla del mail de presentar aportación, puede que esté mal construida");
						}
					}
					enviarMailRender(idSolicitud);

				}
				
			}
		