package controllers;

import java.io.StringWriter;

import jj.play.org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
import messages.Messages;
import models.MensajeInicio;
import play.mvc.Util;
import utils.StringUtils;
import validation.CustomValidation;
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
	
}
