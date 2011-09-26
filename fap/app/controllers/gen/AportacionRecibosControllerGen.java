
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
			
			
			public class AportacionRecibosControllerGen extends GenericController {

				public static void index(Long idSolicitud){
					SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);

					renderTemplate( "gen/AportacionRecibos/AportacionRecibos.html" , solicitud);
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "AportacionRecibosControllerGen");
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
			
				
	public static void tablarecibosAportados(Long idSolicitud, Long idEntidad){
		
			Long id = idSolicitud != null? idSolicitud : idEntidad;
			java.util.List<Documento> rows = Documento.find( "select documento from SolicitudGenerica solicitud join solicitud.documentacionAportada.documentos documento where solicitud.id=?", id ).fetch();
			
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered);
			renderJSON(response.toJSON("fechaSubida", "urlDescarga", "id"));
				
	}
		
			@Util
			protected static boolean permisosave(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void save(Long idSolicitud){
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
				
				saveRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void saveRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "AportacionRecibosController.index" , idSolicitud);
			}
		
			}
		