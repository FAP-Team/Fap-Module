package controllers;

import messages.Messages;
import models.Agente;
import models.Respuesta;
import play.mvc.Util;
import services.VerificarDatosService;
import services.VerificarDatosServiceException;
import verificacion.VerificacionUtils;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PeticionRecuperadaControllerGen;
import es.gobcan.platino.servicios.svd.*;

public class PeticionRecuperadaController extends PeticionRecuperadaControllerGen {
//	public static void index(String accion) {
//		if (accion == null)
//			accion = getAccion();
//		if (!permiso(accion)) {
//			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
//			renderTemplate("fap/PeticionRecuperada/PeticionRecuperada.html");
//		}
//
//		Agente logAgente = AgenteController.getAgente();
//		log.info("Visitando página: " + "fap/PeticionRecuperada/PeticionRecuperada.html" + " Agente: " + logAgente);
//		renderTemplate("fap/PeticionRecuperada/PeticionRecuperada.html", accion);
//	}

	public static void index(String accion, Long idRespuesta) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PeticionRecuperada/PeticionRecuperada.html");
		}

		Respuesta respuesta = null;
		if ("crear".equals(accion)) {
			respuesta = PeticionRecuperadaController.getRespuesta();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				respuesta.save();
				idRespuesta = respuesta.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			respuesta = PeticionRecuperadaController.getRespuesta(idRespuesta);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/PeticionRecuperada/PeticionRecuperada.html" + " Agente: " + logAgente);
		renderTemplate("fap/PeticionRecuperada/PeticionRecuperada.html", accion, idRespuesta, respuesta);
	}

	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static boolean recuperarPeticion(String codigoPeticion, String uidUsuario) {
		if ((codigoPeticion != null) && (uidUsuario != null)){
			//String accion = getAccion();
			//redirect("PeticionRecuperadaController.index");
			//return true;
			try{
			VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
			es.gobcan.platino.servicios.svd.Respuesta response = verificarDatosService.peticionRecover(uidUsuario, codigoPeticion);
			
			Respuesta respuesta = VerificacionUtils.convertRespuestaSvdToRespuesta(response);
			//respuesta = VerificacionUtils.convertRespuestaSvdToRespuesta(response);
			System.out.println("Codigo peticion: " + codigoPeticion);
			System.out.println("UID Usuario: " + uidUsuario);
			System.out.println("Codigo certificado " + respuesta.atributos.codigoCertificado);
			return true;
			//String accion = getAccion();
			//Agente logAgente = AgenteController.getAgente();
			//log.info("Visitando página: " + "fap/PeticionRecuperada/PeticionRecuperadab.html" + " Agente: " + logAgente);
			//renderTemplate("fap/PeticionRecuperada/PeticionRecuperadab.html", accion, respuesta);	
			}
			catch(VerificarDatosServiceException e){
				play.Logger.error("No se han podido resolver la petición. Causa: " + e.getMessage());
				return false;
			}
			
		}
		else{
			return false;
			//String accion = getAccion();
			//Agente logAgente = AgenteController.getAgente();
			//log.info("Visitando página: " + "fap/PeticionRecuperada/PeticionRecuperada.html" + " Agente: " + logAgente);
			//renderTemplate("fap/PeticionRecuperada/PeticionRecuperada.html", accion);
		} 


	}
}
