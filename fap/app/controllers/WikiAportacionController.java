package controllers;

import java.util.Map;

import messages.Messages;
import models.MensajeAportacion;

import org.apache.log4j.Logger;

import play.mvc.After;
import play.mvc.Before;
import play.mvc.Util;
import security.Accion;
import validation.CustomValidation;
import controllers.gen.WikiAportacionControllerGen;

public class WikiAportacionController extends WikiAportacionControllerGen {

	protected static Logger log = Logger.getLogger("Paginas");

	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acciÃ³n");
			renderTemplate("fap/Admin/WikiAportacion.html");
		}

		MensajeAportacion mensajeAportacion = WikiAportacionController.getMensajeAportacion();
		log.info("Visitando página: " + "fap/Admin/WikiAportacion.html");
		renderTemplate("fap/Admin/WikiAportacion.html", accion, mensajeAportacion);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(MensajeAportacion mensajeAportacion) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		MensajeAportacion dbMensajeAportacion = WikiAportacionController.getMensajeAportacion();

		WikiAportacionController.WikiAportacionBindReferences(mensajeAportacion);

		if (!Messages.hasErrors()) {

			WikiAportacionController.WikiAportacionValidateCopy("editar", dbMensajeAportacion, mensajeAportacion);

		}

		if (!Messages.hasErrors()) {
			WikiAportacionController.editarValidateRules(dbMensajeAportacion, mensajeAportacion);
		}
		if (!Messages.hasErrors()) {
			dbMensajeAportacion.save();
			log.info("Acción Editar de página: " + "fap/Admin/WikiAportacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "fap/Admin/WikiAportacion.html" + " , intentada sin éxito (Problemas de Validación)");
		WikiAportacionController.editarRender();
	}

	@Util
	public static void WikiAportacionValidateCopy(String accion, MensajeAportacion dbMensajeAportacion, MensajeAportacion mensajeAportacion) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("mensajeAportacion", mensajeAportacion);
		dbMensajeAportacion.contenido = mensajeAportacion.contenido;
		dbMensajeAportacion.habilitar = mensajeAportacion.habilitar;

	}

	@Util
	public static boolean permiso(String accion) {

		if (Accion.parse(accion) == null)
			return false;
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		return secure.checkAcceso("administrador", accion, ids, null);

	}

	@Util
	public static String getAccion() {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		return secure.getPrimeraAccion("administrador", ids, null);

	}

	@Util
	public static void editarRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("WikiAportacionController.index", "editar");
		}
		Messages.keep();
		redirect("WikiAportacionController.index", "editar");
	}

	@Util
	public static void editarValidateRules(MensajeAportacion dbMensajeAportacion, MensajeAportacion mensajeAportacion) {
		//Sobreescribir para validar las reglas de negocio
	}

	@Util
	public static void WikiAportacionBindReferences(MensajeAportacion mensajeAportacion) {

	}

	@Util
	public static MensajeAportacion getMensajeAportacion() {
		return MensajeAportacion.get(MensajeAportacion.class);
	}

	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "WikiAportacionControllerGen");
	}

	@After(only = { "WikiAportacionController.editar", "WikiAportacionControllerGen.editar" })
	protected static void setEntidadesProcesada() {
		unsetEntidadesProcesando();
	}

	@Before(only = { "WikiAportacionController.editar", "WikiAportacionControllerGen.editar" })
	protected static void setEntidadesProcesandose() {
		setEntidadesProcesando();
	}
	
}
