
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
			
			
			public class AportacionPresentarControllerGen extends GenericController {

				public static void index(Long idSolicitud){
					SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);

					renderTemplate( "gen/AportacionPresentar/AportacionPresentar.html" , solicitud);
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "AportacionPresentarControllerGen");
				}
	
		
				@Util
				protected static SolicitudGenerica getSolicitudGenerica(Long idSolicitud){
					SolicitudGenerica solicitud = null;
					if(idSolicitud == null){
						Messages.fatal("Falta parámetro idSolicitud");
					}else{
						solicitud = SolicitudGenerica.findById(idSolicitud);
						if(solicitud == null){
							Messages.fatal("Error al recuperar SolicitudGenerica");
						}
					}
					return solicitud;
				}
			
				
			@Util
			protected static boolean permisomodificarBorrador(String accion) {
				
				if (accion == null) return false;
	
				boolean permiso;
				if (accion.equals("crear")) {
					permiso = secure.PermissionFap.aportacionModificar("create", (Map<String, Long>) tags.TagMapStack.top("idParams"), null);
					return permiso;
				}else if (accion.equals("borrar")) {
					permiso =  secure.PermissionFap.aportacionModificar("delete", (Map<String, Long>) tags.TagMapStack.top("idParams"), null);
					return permiso;
				}else if (accion.equals("editar")) {
					permiso = secure.PermissionFap.aportacionModificar("update", (Map<String, Long>) tags.TagMapStack.top("idParams"), null);
					return permiso;
				}else if (accion.equals("leer")) {
					permiso = secure.PermissionFap.aportacionModificar("read", (Map<String, Long>) tags.TagMapStack.top("idParams"), null);
					return permiso;
				}else{
					return secure.PermissionFap.aportacionModificar(accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null);
				 }
			
			}
		
			
			
			public static void modificarBorrador(Long idSolicitud){
				checkAuthenticity();
				if (permisomodificarBorrador("update") || permisomodificarBorrador("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				modificarBorradorRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void modificarBorradorRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				
				if(Messages.hasErrors()){
					redirect( "AportacionPresentarController.index" , idSolicitud);
				}else{
					redirect( "AportacionController.index" , idSolicitud);
				}			
			
			}
		
			@Util
			protected static boolean permisopresentar(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void presentar(Long idSolicitud, platino.Firma firma){
				checkAuthenticity();
				if (permisopresentar("update") || permisopresentar("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				presentarRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void presentarRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				
				if(Messages.hasErrors()){
					redirect( "AportacionPresentarController.index" , idSolicitud);
				}else{
					redirect( "AportacionRecibosController.index" , idSolicitud);
				}			
			
			}
		
			}
		