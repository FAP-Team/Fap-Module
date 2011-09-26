
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
			
			
			public class UsuariosControllerGen extends GenericController {

				public static void index(){
					

					renderTemplate( "gen/Usuarios/Usuarios.html" );
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "UsuariosControllerGen");
				}
	
		
				
	public static void tablalistaUsuarios(Long idAgente, Long idEntidad){
		
			Long id = idAgente != null? idAgente : idEntidad;
			java.util.List<Agente> rows = Agente.find( "select agente from Agente agente" ).fetch();
			
		List<Agente> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<Agente> response = new tables.TableRenderResponse<Agente>(rowsFiltered);
			renderJSON(response.toJSON("id", "username", "name", "roles", "email"));
				
	}
		
			@Util
			protected static boolean permisosave(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void save(){
				checkAuthenticity();
				if (permisosave("update") || permisosave("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				saveRender();

			}
			
			

			
		
			@Util
			public static void saveRender(){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "UsuariosController.index" );
			}
		
			}
		