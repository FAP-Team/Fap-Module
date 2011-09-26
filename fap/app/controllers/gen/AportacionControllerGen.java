
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
			
			
			public class AportacionControllerGen extends GenericController {

				public static void index(Long idSolicitud){
					Documento documento = getDocumento();
SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);

					renderTemplate( "gen/Aportacion/Aportacion.html" , documento, solicitud);
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "AportacionControllerGen");
				}
	
		
				@Util
				protected static Documento getDocumento(){
					return new Documento();
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
			
				
	public static void tabladocumentos(Long idSolicitud, Long idEntidad){
		
			Long id = idSolicitud != null? idSolicitud : idEntidad;
			java.util.List<Documento> rows = Documento.find( "select documento from SolicitudGenerica solicitud join solicitud.aportaciones.actual.documentos documento where solicitud.id=?", id ).fetch();
			
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered);
			renderJSON(response.toJSON("fechaSubida", "descripcion", "urlDescarga", "id"));
				
	}
		
			@Util
			protected static boolean permisoaddDocumento(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			@Util
			protected static void addDocumentoValidateCopy(Documento dbDocumento, Documento documento, java.io.File fileAportacion){
				CustomValidation.clearValidadas();
				CustomValidation.required("documento", documento);
dbDocumento.tipo = documento.tipo;
dbDocumento.descripcion = documento.descripcion;

				
		if(fileAportacion == null) validation.addError("fileAportacion", "Archivo requerido");

		if(!validation.hasErrors()){
			try {
				aed.AedClient.saveDocumentoTemporal(dbDocumento, fileAportacion);
			}catch(es.gobcan.eadmon.aed.ws.AedExcepcion e){
				validation.addError("", "Error al subir el documento al Archivo Electrónico");
			}
		}
		
			}
		
			
			public static void addDocumento(Long idSolicitud, Documento documento, java.io.File fileAportacion){
				checkAuthenticity();
				if (permisoaddDocumento("update") || permisoaddDocumento("create")) {
				
					Documento dbDocumento = getDocumento();
				
					addDocumentoValidateCopy(dbDocumento, documento, fileAportacion);

					if(!validation.hasErrors()){
						addDocumentoValidateRules(dbDocumento, documento, fileAportacion);
					}
					
					if(!validation.hasErrors()){
						dbDocumento.save(); Logger.info("Guardando documento " + dbDocumento.id);
				
		
						SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
						dbSolicitud.aportaciones.actual.documentos.add(dbDocumento);
						dbSolicitud.save();
			
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				addDocumentoRender(idSolicitud);

			}
			
			
			@Util
			protected static void addDocumentoValidateRules(Documento dbDocumento, Documento documento, java.io.File fileAportacion){
				//Sobreescribir para validar las reglas de negocio
			}
		

			
		
			@Util
			public static void addDocumentoRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "AportacionController.index" , idSolicitud);
			}
		
			@Util
			protected static boolean permisopresentar(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void presentar(Long idSolicitud){
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
					redirect( "AportacionController.index" , idSolicitud);
				}else{
					redirect( "AportacionPresentarController.index" , idSolicitud);
				}			
			
			}
		
			}
		