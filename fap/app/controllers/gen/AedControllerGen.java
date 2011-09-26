
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
			
			
			public class AedControllerGen extends GenericController {

				public static void index(){
					

					renderTemplate( "gen/Aed/Aed.html" );
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "AedControllerGen");
				}
	
		
				
			@Util
			protected static boolean permisotiposDeDocumentos(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void tiposDeDocumentos(){
				checkAuthenticity();
				if (permisotiposDeDocumentos("update") || permisotiposDeDocumentos("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				tiposDeDocumentosRender();

			}
			
			

			
		
			@Util
			public static void tiposDeDocumentosRender(){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "AedController.index" );
			}
		
			}
		