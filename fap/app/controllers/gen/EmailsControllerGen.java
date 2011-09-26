
			package controllers.gen;

			import play.*;
			import play.mvc.*;
			import controllers.fap.*;
			import tags.ReflectionUtils;
			import validation.*;
			import models.*;
			import java.util.*;
			import messages.Messages;
			import java.lang.reflect.Field;
			
			
			public class EmailsControllerGen extends GenericController {

				public static void index(){
					

					renderTemplate( "gen/Emails/Emails.html" );
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "EmailsControllerGen");
				}
	
		
				
	public static void tablaemails(Long idMail, Long idEntidad){
		
			Long id = idMail != null? idMail : idEntidad;
			java.util.List<Mail> rows = Mail.find( "select mail from Mail mail" ).fetch();
			
		List<Mail> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<Mail> response = new tables.TableRenderResponse<Mail>(rowsFiltered);
			renderJSON(response.toJSON("idMail", "content", "id"));
				
	}
		
			@Util
			protected static boolean permisoactualizarDesdeFichero(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void actualizarDesdeFichero(){
				checkAuthenticity();
				if (permisoactualizarDesdeFichero("update") || permisoactualizarDesdeFichero("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				actualizarDesdeFicheroRender();

			}
			
			

			
		
			@Util
			public static void actualizarDesdeFicheroRender(){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "EmailsController.index" );
			}
		
			}
		