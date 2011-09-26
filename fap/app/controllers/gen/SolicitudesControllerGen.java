
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
			
			
			public class SolicitudesControllerGen extends GenericController {

				public static void index(){
					

					renderTemplate( "gen/Solicitudes/Solicitudes.html" );
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "SolicitudesControllerGen");
				}
	
		
				
	public static void tablalistaSolicitudes(Long idSolicitud, Long idEntidad){
		
			Long id = idSolicitud != null? idSolicitud : idEntidad;
			java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find( "select solicitud from SolicitudGenerica solicitud" ).fetch();
			
		Map<String, Long> ids = new HashMap<String, Long>();
		ids.put("idSolicitud", idSolicitud);
		List<SolicitudGenerica> rowsFiltered = new ArrayList<SolicitudGenerica>();
		for(SolicitudGenerica solicitud: rows){
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("solicitud", solicitud);
			if (secure.PermissionFap.listaSolicitudes("read", ids, vars)) {
				rowsFiltered.add(solicitud);
			}
		}
		
			
			tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered);
			renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "estadoUsuario", "solicitante.numeroId", "solicitante.nombreCompleto"));
				
	}
		
			@Util
			protected static boolean permisonuevaSolicitud(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void nuevaSolicitud(){
				checkAuthenticity();
				if (permisonuevaSolicitud("update") || permisonuevaSolicitud("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				nuevaSolicitudRender();

			}
			
			

			
		
			@Util
			public static void nuevaSolicitudRender(){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "SolicitudesController.index" );
			}
		
			}
		