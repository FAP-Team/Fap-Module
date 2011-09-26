
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
			
			@With({PropertiesFap.class, MessagesController.class, AgenteController.class})
			public class SoporteControllerGen extends Controller {

				public static void index(){
					Incidencia incidencia = getIncidencia();

					renderTemplate( "gen/Soporte/Soporte.html" , incidencia);
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "SoporteControllerGen");
				}
	
		
				@Util
				protected static Incidencia getIncidencia(){
					return new Incidencia();
				}
			
				
			@Util
			protected static boolean permisoincidencia(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			@Util
			protected static void incidenciaValidateCopy(Incidencia dbIncidencia, Incidencia incidencia){
				CustomValidation.clearValidadas();
				CustomValidation.valid("incidencia", incidencia);
CustomValidation.required("incidencia.email", incidencia.email);
dbIncidencia.email = incidencia.email;
CustomValidation.required("incidencia.telefono", incidencia.telefono);
dbIncidencia.telefono = incidencia.telefono;
CustomValidation.required("incidencia.nombre", incidencia.nombre);
dbIncidencia.nombre = incidencia.nombre;
CustomValidation.required("incidencia.apellidos", incidencia.apellidos);
dbIncidencia.apellidos = incidencia.apellidos;
CustomValidation.required("incidencia.asunto", incidencia.asunto);
dbIncidencia.asunto = incidencia.asunto;
CustomValidation.required("incidencia.texto", incidencia.texto);
dbIncidencia.texto = incidencia.texto;

				
			}
		
			
			public static void incidencia(Incidencia incidencia){
				checkAuthenticity();
				if (permisoincidencia("update") || permisoincidencia("create")) {
				
					Incidencia dbIncidencia = getIncidencia();
				
					incidenciaValidateCopy(dbIncidencia, incidencia);

					if(!validation.hasErrors()){
						incidenciaValidateRules(dbIncidencia, incidencia);
					}
					
					if(!validation.hasErrors()){
						dbIncidencia.save(); Logger.info("Guardando incidencia " + dbIncidencia.id);
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				incidenciaRender();

			}
			
			
			@Util
			protected static void incidenciaValidateRules(Incidencia dbIncidencia, Incidencia incidencia){
				//Sobreescribir para validar las reglas de negocio
			}
		

			
		
			@Util
			public static void incidenciaRender(){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "SoporteController.index" );
			}
		
			}
		