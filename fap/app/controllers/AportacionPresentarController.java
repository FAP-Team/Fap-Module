package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import messages.Messages;
import models.*;
import play.mvc.Util;
import services.FirmaService;
import services.RegistroException;
import services.RegistroService;
import controllers.gen.AportacionPresentarControllerGen;

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
			
			presentarSinRegistrarValidateCopy(dbSolicitud, solicitud, firma);
			
			Aportacion aportacion = dbSolicitud.aportaciones.actual;
			
			if ((aportacion.fechaAportacionSinRegistro == null) || (aportacion.fechaAportacionSinRegistro.isAfterNow())) {
				System.out.println("-> "+aportacion.fechaAportacionSinRegistro);
		        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		        Date date = new Date();
				Messages.error("La fecha de incorporación debe ser anterior a "+dateFormat.format(date));
			}
			
			
			if (!Messages.hasErrors()){
				play.Logger.info("Se procede a aportar sin registrar en la solicitud: "+dbSolicitud.id);
				play.Logger.info("El estado es "+dbSolicitud.estado);
				
				
			}

			
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			/* no se hace aqui Messages.keep(); */
		}
		
		presentarSinRegistrarRender(idSolicitud);

	}
	
}
