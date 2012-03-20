package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import aed.AedClient;

import messages.Messages;
import models.*;
import play.Logger;
import play.mvc.Util;
import services.FirmaService;
import services.RegistroException;
import services.RegistroService;
import validation.CustomValidation;
import controllers.gen.AportacionPresentarControllerGen;
import emails.Mails;

public class AportacionPresentarController extends AportacionPresentarControllerGen {

	public static void index(Long idSolicitud){
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		Aportacion aportacion = solicitud.aportaciones.actual;
		
		if(aportacion.estado == null){
			//Si la aportación no esta preparada, vuelve a la página para subir documentos
			Messages.warning("Su aportación de documentación no está preparada para el registro. Pulse el botón 'Registrar Aportacion'");
			Messages.keep();
			redirect("AportacionController.index", idSolicitud);
		}else{
			renderTemplate( "gen/AportacionPresentar/AportacionPresentar.html" , solicitud);
		}
	}
	
	public static void modificarBorrador(Long idSolicitud){
		checkAuthenticity();
		if (permisomodificarBorrador("update") || permisomodificarBorrador("create")) {
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
		if (permisopresentar("update") || permisopresentar("create")) {
			
			SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
			Aportacion aportacion = solicitud.aportaciones.actual;
			
			if(aportacion.estado == null){
				Messages.error("La solicitud no está preparada para registrar");
			}
			
			//Firma si es necesario
			if(!Messages.hasErrors() && aportacion.estado.equals("borrador")){
				play.Logger.info("Calculando firmantes");
				
				List<Firmante> firmantes = new ArrayList<Firmante>();
				
				FirmaService.calcularFirmantes(solicitud.solicitante, firmantes);
				
				play.Logger.info("Firmantes " + firmantes);
				
				FirmaService.firmar(solicitud.aportaciones.actual.oficial, firmantes, firma);
				
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
					RegistroService.registrarAportacionActual(solicitud);
				} catch (RegistroException e) {
					e.printStackTrace();
					Messages.error("Se produjo un error al intentar registrar la aportación, inténtelo de nuevo.");
				}
			}
	
			if(!Messages.hasErrors()){
				Messages.ok("Su solicitud de aportación de documentación se registró correctamente");
				try{
					Mails.enviar("aportacionRealizada", solicitud);
				} catch (IllegalArgumentException e){
					Logger.error("No se encontró el ID del mail en la base de datos");
				} catch (Exception e){
					Logger.error("Problemas con la plantilla del mail de presentar aportación, puede que esté mal construida");
				}
			}
			
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		presentarRender(idSolicitud);
	}
	
	
	/** Presenta la aportación de documentación sin registrar los documentos.
	 * Deberá realizarlo únicamente un gestor, administrador o revisor. */
	public static void presentarSinRegistrar(Long idSolicitud, SolicitudGenerica solicitud, platino.Firma firma){
		checkAuthenticity();
		if (permisopresentarSinRegistrar("update") || permisopresentarSinRegistrar("create")) {
		
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			

			CustomValidation.required("solicitud.aportaciones.actual.fechaAportacionSinRegistro", solicitud.aportaciones.actual.fechaAportacionSinRegistro);
			dbSolicitud.aportaciones.actual.fechaAportacionSinRegistro = solicitud.aportaciones.actual.fechaAportacionSinRegistro;
			
			Aportacion aportacion = dbSolicitud.aportaciones.actual;
			
			//No Registra la solicitud
			if(!Messages.hasErrors()){
				try {
					RegistroService.noRegistrarAportacionActual(dbSolicitud);
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
			redirect( "AportacionPresentarController.index" , idSolicitud);
		}else{
			redirect( "AportacionAportadosController.index" , idSolicitud);
		}			
	
	}
	
}
