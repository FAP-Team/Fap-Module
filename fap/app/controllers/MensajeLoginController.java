package controllers;

import java.io.StringWriter;

import com.google.inject.Inject;

import jj.play.org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
import messages.Messages;
import models.MensajeInicio;
import play.mvc.Util;
import services.FirmaService;
import services.GestorDocumentalService;
import services.NotificacionService;
import services.platino.PlatinoFirmaServiceImpl;
import utils.StringUtils;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.gen.MensajeLoginControllerGen;

public class MensajeLoginController extends MensajeLoginControllerGen {
	
	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/Admin/MensajeLogin.html");
		}

		MensajeInicio mensajeInicio = MensajeLoginController.getMensajeInicio();
		log.info("Visitando página: " + "fap/Admin/MensajeLogin.html");
		renderTemplate("fap/Admin/MensajeLogin.html", accion, mensajeInicio);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(MensajeInicio mensajeInicio) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		MensajeInicio dbMensajeInicio = MensajeLoginController.getMensajeInicio();

		MensajeLoginController.MensajeLoginBindReferences(mensajeInicio);

		if (!Messages.hasErrors()) {

			MensajeLoginController.MensajeLoginValidateCopy("editar", dbMensajeInicio, mensajeInicio);

		}

		if (!Messages.hasErrors()) {
			MensajeLoginController.editarValidateRules(dbMensajeInicio, mensajeInicio);
		}
		if (!Messages.hasErrors()) {
			dbMensajeInicio.save();
			log.info("Acción Editar de página: " + "fap/Admin/MensajeLogin.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "fap/Admin/MensajeLogin.html" + " , intentada sin éxito (Problemas de Validación)");
		MensajeLoginController.editarRender();
	}
	
	public static StringBuffer convertData(String contenido){
		return StringUtils.getParsedText(contenido);
	}
	
	/*
	 * Retorna false si el servicio Platino no está disponible, true en caso contrario. 
	 * 
	 */
	private static boolean platinoIsConfigured() {
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		return firmaService.isConfigured();
	}
	
	/*
	 * Retorna false si el servicio Notificación no está disponible, true en caso contrario. 
	 * 
	 */
	private static boolean notificacionIsConfigured() {
		NotificacionService notificacionService = InjectorConfig.getInjector().getInstance(NotificacionService.class);
		return notificacionService.isConfigured();
	}
	
	/*
	 * Retorna false si el servicio Gestor Documental no está disponible, true en caso contrario.  
	 * 
	 */
	private static boolean gestorDocumentalIsConfigured() {
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		return gestorDocumentalService.isConfigured();
	}
	

	/*
	 * Retorna un string con los servicios que no están disponibles, o una cadena vacía si están todos activos.
	 * Usado para insertar un mensaje de aviso en la pantalla de login. 
	 * 
	 */
	public static String servicesIsConfigured() {
		String msg = "";
		if (!platinoIsConfigured())
			msg += "Platino";
		if (!notificacionIsConfigured()) {
			if (msg.isEmpty())
				msg += "Notificación";
			else
				msg += ", Notificación";
		}
		if (!gestorDocumentalIsConfigured()) {
			if (msg.isEmpty())
				msg += "Gestor Documental";
			else
				msg += ", Gestor Documental";
		}
		return msg;
	}
}
