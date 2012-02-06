package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.Aportacion;
import models.Firmante;
import models.SolicitudGenerica;
import play.mvc.Util;
import services.FirmaService;
import services.RegistroServiceException;
import services.RegistroService;
import validation.CustomValidation;
import controllers.gen.AportacionPresentarControllerGen;

public class AportacionPresentarController extends AportacionPresentarControllerGen {

	@Inject
	static FirmaService firmaService;
	
	@Inject
	static RegistroService registroService;

	public static void index(String accion, Long idSolicitud){
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		Aportacion aportacion = solicitud.aportaciones.actual;
		
		if(aportacion.estado == null){
			//Si la aportación no esta preparada, vuelve a la página para subir documentos
			Messages.warning("Su aportación de documentación no está preparada para el registro. Pulse el botón 'Registrar Aportacion'");
			Messages.keep();
			redirect("AportacionController.index", accion, idSolicitud);
		}else{
			renderTemplate("gen/AportacionPresentar/AportacionPresentar.html", accion, idSolicitud, solicitud);
		}
	}
	
	public static void modificarBorrador(Long idSolicitud){
		checkAuthenticity();
		if (permisoModificarBorrador("editar") || permisoModificarBorrador("crear")) {
			SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
			Aportacion aportacion = solicitud.aportaciones.actual;
			aportacion.estado = null;
			aportacion.save();
			Messages.ok("Ahora puede modificar los datos de la solicitud de aportación.");
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			Messages.keep();
		}
		modificarBorradorRender(idSolicitud);
	}
	
	/**
	 * Firma y registra la solicitud de aportación de documentación
	 * @param idSolicitud
	 * @param firma
	 */
	public static void presentar(Long idSolicitud, platino.Firma firma) {
		checkAuthenticity();
		if (permisoPresentar("editar") || permisoPresentar("crear")) {
			
			SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
			Aportacion aportacion = solicitud.aportaciones.actual;
			
			if(aportacion.estado == null){
				Messages.error("La solicitud no está preparada para registrar");
			}
			
			//Firma si es necesario
			if(!Messages.hasErrors() && aportacion.estado.equals("borrador")){
				play.Logger.info("Calculando firmantes");
				
				List<Firmante> firmantes = new ArrayList<Firmante>();
				
				firmaService.calcularFirmantes(solicitud.solicitante, firmantes);
				
				play.Logger.info("Firmantes " + firmantes);
				
				firmaService.firmar(solicitud.aportaciones.actual.oficial, firmantes, firma);
				
				play.Logger.info("Firmada");
				
				//La solicitud se firmó correctamente y la firma ya está guardada en el AED
				if(!Messages.hasErrors()){
					aportacion.estado = "firmada";
					aportacion.save();
				}
			}
			
			//Registra la solicitud
			if(!Messages.hasErrors()){
				try {
					registroService.registrarAportacionActual(solicitud);
				} catch (RegistroServiceException e) {
					e.printStackTrace();
					Messages.error("Se produjo un error al intentar registrar la aportación, inténtelo de nuevo.");
				}
			}
	
			if(!Messages.hasErrors()){
				Messages.ok("Su solicitud de aportación de documentación se registró correctamente");
			}
			
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		presentarRender(idSolicitud);
	}
	
	
	/** Presenta la aportación de documentación sin registrar los documentos.
	 * Deberá realizarlo únicamente un gestor, administrador o revisor. */
	public static void presentarSinRegistrar(Long idSolicitud, SolicitudGenerica solicitud){
		checkAuthenticity();
		if (permisoPresentarSinRegistrar("editar") || permisoPresentarSinRegistrar("crear")) {
		
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			

			CustomValidation.required("solicitud.aportaciones.actual.fechaAportacionSinRegistro", solicitud.aportaciones.actual.fechaAportacionSinRegistro);
			dbSolicitud.aportaciones.actual.fechaAportacionSinRegistro = solicitud.aportaciones.actual.fechaAportacionSinRegistro;
			
			Aportacion aportacion = dbSolicitud.aportaciones.actual;
			
			//No Registra la solicitud
			if(!Messages.hasErrors()){
				try {
					registroService.noRegistrarAportacionActual(dbSolicitud);
				} catch (Exception e) {
					e.printStackTrace();
					Messages.error("Se produjo un error al intentar aportar sin registrar la documentación, inténtelo de nuevo.");
				}
			}
			
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			/* no se hace aqui Messages.keep(); */
		}
		
		presentarSinRegistrarRender(idSolicitud);

	}
	
	/**
	 * Redireccionamos a la página de documentos aportados, ya que por defecto redireccionaba
	 * a la página de recibos
	 * @param idSolicitud
	 */
	@Util
	public static void presentarSinRegistrarRender(Long idSolicitud){
		if (!Messages.hasMessages()) {
			Messages.ok("Página guardada correctamente");
		}		
		Messages.keep();
		if(Messages.hasErrors()){
			redirect("AportacionPresentarController.index", "editar", idSolicitud);
		}else{
			redirect("AportacionAportadosController.index", "editar", idSolicitud);
		}			
	
	}
	
}
