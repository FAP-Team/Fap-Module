
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
			
			
			public class RequerimientosControllerGen extends GenericController {

				public static void index(){
					Quartz quartz = getQuartz();

					renderTemplate( "gen/Requerimientos/Requerimientos.html" , quartz);
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "RequerimientosControllerGen");
				}
	
		
				@Util
				protected static Quartz getQuartz(){
					return Quartz.get(Quartz.class);
				}
			
				
			@Util
			protected static boolean permisosave(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			@Util
			protected static void saveValidateCopy(Quartz dbQuartz, Quartz quartz){
				CustomValidation.clearValidadas();
				CustomValidation.valid("quartz", quartz);
dbQuartz.execute = quartz.execute;
dbQuartz.mostrarTodasSolicitudes = quartz.mostrarTodasSolicitudes;
dbQuartz.mostrarSolicitudesRequeridas = quartz.mostrarSolicitudesRequeridas;
dbQuartz.ejecutarCambioDeFecha = quartz.ejecutarCambioDeFecha;
dbQuartz.cambiarEstadoPlazoVencido = quartz.cambiarEstadoPlazoVencido;
dbQuartz.sendMail = quartz.sendMail;

				
			}
		
			
			public static void save(Quartz quartz){
				checkAuthenticity();
				if (permisosave("update") || permisosave("create")) {
				
					Quartz dbQuartz = getQuartz();
				
					saveValidateCopy(dbQuartz, quartz);

					if(!validation.hasErrors()){
						saveValidateRules(dbQuartz, quartz);
					}
					
					if(!validation.hasErrors()){
						dbQuartz.save(); Logger.info("Guardando quartz " + dbQuartz.id);
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				saveRender();

			}
			
			
			@Util
			protected static void saveValidateRules(Quartz dbQuartz, Quartz quartz){
				//Sobreescribir para validar las reglas de negocio
			}
		

			
		
			@Util
			public static void saveRender(){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "RequerimientosController.index" );
			}
		
			}
		