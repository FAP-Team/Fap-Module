
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
			
			
			public class VerificacionControllerGen extends GenericController {

				public static void index(Long idSolicitud){
					SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);

					renderTemplate( "gen/Verificacion/Verificacion.html" , solicitud);
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "VerificacionControllerGen");
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
			protected static boolean permisoiniciarVerificacion(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void iniciarVerificacion(Long idSolicitud){
				checkAuthenticity();
				if (permisoiniciarVerificacion("update") || permisoiniciarVerificacion("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				iniciarVerificacionRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void iniciarVerificacionRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
	public static void tablaverificacionDocumentos(Long idSolicitud, Long idEntidad){
		
			Long id = idSolicitud != null? idSolicitud : idEntidad;
			java.util.List<VerificacionDocumento> rows = VerificacionDocumento.find( "select verificacionDocumento from SolicitudGenerica solicitud join solicitud.verificacion.documentos verificacionDocumento where solicitud.id=?", id ).fetch();
			
		List<VerificacionDocumento> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<VerificacionDocumento> response = new tables.TableRenderResponse<VerificacionDocumento>(rowsFiltered);
			renderJSON(response.toJSON("etiquetaTipoDocumento", "descripcion", "fechaPresentacion", "version", "estadoDocumentoVerificacion", "id"));
				
	}
		
	public static void tablaexclusion(Long idSolicitud, Long idEntidad){
		
			Long id = idSolicitud != null? idSolicitud : idEntidad;
			java.util.List<Exclusion> rows = Exclusion.find( "select exclusion from SolicitudGenerica solicitud join solicitud.verificacion.codigosExclusion exclusion where solicitud.id=?", id ).fetch();
			
		List<Exclusion> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<Exclusion> response = new tables.TableRenderResponse<Exclusion>(rowsFiltered);
			renderJSON(response.toJSON("descripcionCorta", "id"));
				
	}
		
			@Util
			protected static boolean permisognuevoRequerimientoBorradorPreliminar(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void gnuevoRequerimientoBorradorPreliminar(Long idSolicitud){
				checkAuthenticity();
				if (permisognuevoRequerimientoBorradorPreliminar("update") || permisognuevoRequerimientoBorradorPreliminar("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				gnuevoRequerimientoBorradorPreliminarRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void gnuevoRequerimientoBorradorPreliminarRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
			@Util
			protected static boolean permisofinalizarTemporal(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void finalizarTemporal(Long idSolicitud){
				checkAuthenticity();
				if (permisofinalizarTemporal("update") || permisofinalizarTemporal("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				finalizarTemporalRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void finalizarTemporalRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
			@Util
			protected static boolean permisogrequerimientoGuardar(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			@Util
			protected static void grequerimientoGuardarValidateCopy(SolicitudGenerica dbSolicitud, SolicitudGenerica solicitud){
				CustomValidation.clearValidadas();
				CustomValidation.valid("solicitud.verificacion.requerimientoProceso", solicitud.verificacion.requerimientoProceso);
CustomValidation.valid("solicitud.verificacion", solicitud.verificacion);
CustomValidation.valid("solicitud", solicitud);
dbSolicitud.verificacion.requerimientoProceso.motivo = solicitud.verificacion.requerimientoProceso.motivo;

				
			}
		
			
			public static void grequerimientoGuardar(Long idSolicitud, SolicitudGenerica solicitud){
				checkAuthenticity();
				if (permisogrequerimientoGuardar("update") || permisogrequerimientoGuardar("create")) {
				
					SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
				
					grequerimientoGuardarValidateCopy(dbSolicitud, solicitud);

					if(!validation.hasErrors()){
						grequerimientoGuardarValidateRules(dbSolicitud, solicitud);
					}
					
					if(!validation.hasErrors()){
						dbSolicitud.save(); Logger.info("Guardando solicitud " + dbSolicitud.id);
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				grequerimientoGuardarRender(idSolicitud);

			}
			
			
			@Util
			protected static void grequerimientoGuardarValidateRules(SolicitudGenerica dbSolicitud, SolicitudGenerica solicitud){
				//Sobreescribir para validar las reglas de negocio
			}
		

			
		
			@Util
			public static void grequerimientoGuardarRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
			@Util
			protected static boolean permisofrequerimientoSolicitaFirma(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			@Util
			protected static void frequerimientoSolicitaFirmaValidateCopy(SolicitudGenerica dbSolicitud, SolicitudGenerica solicitud){
				CustomValidation.clearValidadas();
				CustomValidation.valid("solicitud.verificacion.requerimientoProceso", solicitud.verificacion.requerimientoProceso);
CustomValidation.valid("solicitud.verificacion", solicitud.verificacion);
CustomValidation.valid("solicitud", solicitud);
CustomValidation.validValueFromTable("solicitud.verificacion.requerimientoProceso.firmante", solicitud.verificacion.requerimientoProceso.firmante);
dbSolicitud.verificacion.requerimientoProceso.firmante = solicitud.verificacion.requerimientoProceso.firmante;

				
			}
		
			
			public static void frequerimientoSolicitaFirma(Long idSolicitud, SolicitudGenerica solicitud){
				checkAuthenticity();
				if (permisofrequerimientoSolicitaFirma("update") || permisofrequerimientoSolicitaFirma("create")) {
				
					SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
				
					frequerimientoSolicitaFirmaValidateCopy(dbSolicitud, solicitud);

					if(!validation.hasErrors()){
						frequerimientoSolicitaFirmaValidateRules(dbSolicitud, solicitud);
					}
					
					if(!validation.hasErrors()){
						dbSolicitud.save(); Logger.info("Guardando solicitud " + dbSolicitud.id);
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				frequerimientoSolicitaFirmaRender(idSolicitud);

			}
			
			
			@Util
			protected static void frequerimientoSolicitaFirmaValidateRules(SolicitudGenerica dbSolicitud, SolicitudGenerica solicitud){
				//Sobreescribir para validar las reglas de negocio
			}
		

			
		
			@Util
			public static void frequerimientoSolicitaFirmaRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
			@Util
			protected static boolean permisognuevoRequerimientoBorrador(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void gnuevoRequerimientoBorrador(Long idSolicitud){
				checkAuthenticity();
				if (permisognuevoRequerimientoBorrador("update") || permisognuevoRequerimientoBorrador("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				gnuevoRequerimientoBorradorRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void gnuevoRequerimientoBorradorRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
			@Util
			protected static boolean permisogrequerimientoFirmarRequerimiento(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void grequerimientoFirmarRequerimiento(Long idSolicitud){
				checkAuthenticity();
				if (permisogrequerimientoFirmarRequerimiento("update") || permisogrequerimientoFirmarRequerimiento("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				grequerimientoFirmarRequerimientoRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void grequerimientoFirmarRequerimientoRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
	public static void tablarequerimientos(Long idSolicitud, Long idEntidad){
		
			Long id = idSolicitud != null? idSolicitud : idEntidad;
			java.util.List<Requerimiento> rows = Requerimiento.find( "select requerimiento from SolicitudGenerica solicitud join solicitud.verificacion.requerimientos requerimiento where solicitud.id=?", id ).fetch();
			
		List<Requerimiento> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<Requerimiento> response = new tables.TableRenderResponse<Requerimiento>(rowsFiltered);
			renderJSON(response.toJSON("numeroGeneralRegistroSalida", "fechaRegistroSalida", "fechaAcceso", "estado", "uriDocJustificanteRequerimiento", "id"));
				
	}
		
			@Util
			protected static boolean permisogfinalizarRequerimiento(String accion) {
				//Sobreescribir para incorporar permisos a mano
			return true;
			}
		
			
			
			public static void gfinalizarRequerimiento(Long idSolicitud){
				checkAuthenticity();
				if (permisogfinalizarRequerimiento("update") || permisogfinalizarRequerimiento("create")) {
				
					
				
					

					if(!validation.hasErrors()){
						
					}
					
					if(!validation.hasErrors()){
						
				
		
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				gfinalizarRequerimientoRender(idSolicitud);

			}
			
			

			
		
			@Util
			public static void gfinalizarRequerimientoRender(Long idSolicitud){
				
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			
				Messages.keep();
				redirect( "VerificacionController.index" , idSolicitud);
			}
		
	public static void tablaregistros(Long idSolicitud, Long idEntidad){
		
			Long id = idSolicitud != null? idSolicitud : idEntidad;
			java.util.List<Documento> rows = Documento.find( "select documento from SolicitudGenerica solicitud join solicitud.documentacionAportada.documentos documento where solicitud.id=?", id ).fetch();
			
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra
			
			tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered);
			renderJSON(response.toJSON("fechaSubida", "urlDescarga", "id"));
				
	}
		
			}
		