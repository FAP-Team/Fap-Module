package controllers;

import java.util.Map;

import messages.Messages;
import models.Agente;
import models.Respuesta;
import models.SolicitudGenerica;
import play.mvc.Util;
import security.Accion;
import services.VerificarDatosService;
import services.VerificarDatosServiceException;
import verificacion.VerificacionUtils;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.VerificacionDatosSVDControllerGen;

public class VerificacionDatosSVDController extends VerificacionDatosSVDControllerGen {

	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/VerificacionDatosSVD/VerificacionDatosSVD.html");
		}

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " Agente: " + logAgente);
		renderTemplate("fap/VerificacionDatosSVD/VerificacionDatosSVD.html", accion);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void recuperaPeticionCodigo(String botonRecuperarPeticion) {
		checkAuthenticity();
		if (!permisoRecuperaPeticionCodigo("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			String accion = getAccion();
			redirect("PeticionRecuperadaController.index", accion);
		}

		if (!Messages.hasErrors()) {
			VerificacionDatosSVDController.recuperaPeticionCodigoValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		VerificacionDatosSVDController.recuperaPeticionCodigoRender();
	}

	@Util
	public static void recuperaPeticionCodigoRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("VerificacionDatosSVDController.index", "editar");
		}
		Messages.keep();
		redirect("VerificacionDatosSVDController.index", "editar");
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void recuperaPDF(String botonRecuperarPeticionPdf) {
		checkAuthenticity();
		if (!permisoRecuperaPDF("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			String accion = getAccion();
			redirect("PeticionPDFRecuperadaController.index", accion);
		}

		if (!Messages.hasErrors()) {
			VerificacionDatosSVDController.recuperaPDFValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		VerificacionDatosSVDController.recuperaPDFRender();
	}

	@Util
	public static void recuperaPDFRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("VerificacionDatosSVDController.index", "editar");
		}
		Messages.keep();
		redirect("VerificacionDatosSVDController.index", "editar");
	}
	
	public static String recuperaPeticion(String codigoPeticion, String uidUsuario) {
		//checkAuthenticity();	
//		try{
//			VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
//			es.gobcan.platino.servicios.svd.Respuesta response = verificarDatosService.peticionRecover(uidUsuario, codigoPeticion);
//			
//			Respuesta respuesta = VerificacionUtils.convertRespuestaSvdToRespuesta(response);
			//respuesta = VerificacionUtils.convertRespuestaSvdToRespuesta(response);
//			System.out.println("Codigo peticion: " + codigoPeticion);
//			System.out.println("UID Usuario: " + uidUsuario);
//			System.out.println("Codigo certificado " + respuesta.atributos.codigoCertificado);
//			String finalff = respuesta.id.toString();
//			SolicitudGenerica solicitud = new SolicitudGenerica();
			Respuesta respuesta = new Respuesta();
			Long longito = new Long (1);
			//respuesta.id = longito;
			respuesta.save();
			System.out.println("ID RESPUESTA: " + respuesta.id.toString());
			String accion = getAccion();
			String identificador = "?idRespuesta=" + respuesta.id.toString() + "&accion=" + accion;
//			
//			String accion = getAccion();
//			//redirect("PeticionRecuperadaController.index", accion);
//			redirect("http://wwww.google.es");
			//return  identificador;
			return identificador;
//			//String accion = getAccion();
//			//Agente logAgente = AgenteController.getAgente();
//			//log.info("Visitando página: " + "fap/PeticionRecuperada/PeticionRecuperadab.html" + " Agente: " + logAgente);
//			//renderTemplate("fap/PeticionRecuperada/PeticionRecuperadab.html", accion, respuesta);	
			//}
//			catch(VerificarDatosServiceException e){
//				play.Logger.error("No se han podido resolver la petición. Causa: " + e.getMessage());
//				VerificacionDatosSVDController.recuperaPeticionCodigoRender();
//				return false;
//			}
	}
	
	
}
