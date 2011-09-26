
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
			
			
			public class TablaDeTablasControllerGen extends GenericController {

				public static void index(){
					

					renderTemplate( "gen/TablaDeTablas/TablaDeTablas.html" );
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "TablaDeTablasControllerGen");
				}
	
		
				
	public static void tablatabladetablas(Long idTableKeyValue, Long idEntidad){
		
			Long id = idTableKeyValue != null? idTableKeyValue : idEntidad;
			java.util.List<TableKeyValue> rows = TableKeyValue.find( "select tableKeyValue from TableKeyValue tableKeyValue" ).fetch();
			
		List<TableKeyValue> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<TableKeyValue> response = new tables.TableRenderResponse<TableKeyValue>(rowsFiltered);
			renderJSON(response.toJSON("table", "key", "value", "id"));
				
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
				redirect( "TablaDeTablasController.index" );
			}
		
			}
		