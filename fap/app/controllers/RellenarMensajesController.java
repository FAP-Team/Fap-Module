package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import play.mvc.Util;
import services.FirmaService;
import services.GestorDocumentalService;
import services.NotificacionService;
import tags.ComboItem;
import utils.StringUtils;
import validation.CustomValidation;
import messages.Messages;
import models.ConfigurarMensaje;
import models.MensajeDocumentacion;
import config.InjectorConfig;
import controllers.gen.RellenarMensajesControllerGen;

public class RellenarMensajesController extends RellenarMensajesControllerGen {
	
	public static void index(String accion, Long idConfigurarMensaje) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/RellenarMensajes/RellenarMensajes.html");
		}

		ConfigurarMensaje configurarMensaje = null;
		if ("crear".equals(accion))
			configurarMensaje = RellenarMensajesController.getConfigurarMensaje();
		else if (!"borrado".equals(accion)){
			configurarMensaje = RellenarMensajesController.getConfigurarMensaje(idConfigurarMensaje);
		}

		log.info("Visitando página: " + "fap/Admin/RellenarMensajes.html");
		renderTemplate("fap/Admin/RellenarMensajes.html", accion, idConfigurarMensaje, configurarMensaje);
	}
	
	@Util
	public static Long crearLogica(ConfigurarMensaje configurarMensaje) {
		checkAuthenticity();
		if (!permiso("crear")) {
		}
		
		ConfigurarMensaje dbConfigurarMensaje = RellenarMensajesController.getConfigurarMensaje();
		RellenarMensajesController.RellenarMensajesBindReferences(configurarMensaje);

		if (!Messages.hasErrors()) {
			RellenarMensajesController.RellenarMensajesValidateCopy("crear", dbConfigurarMensaje, configurarMensaje);
		}
		
		Long idConfigurarMensaje = null;
		if (!Messages.hasErrors()) {
			dbConfigurarMensaje.save();
			idConfigurarMensaje = dbConfigurarMensaje.id;
			log.info("Acción Crear de página: " + "gen/RellenarMensajes/RellenarMensajes.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/RellenarMensajes/RellenarMensajes.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idConfigurarMensaje;
	}
	
	@Util
	public static void editarRender(Long idConfigurarMensaje) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("ConfigurarMensajesController.index", "editar", idConfigurarMensaje);
		}
		Messages.keep();
		redirect("RellenarMensajesController.index", "editar", idConfigurarMensaje);
	}
	
	@Util
	public static void crearRender(Long idConfigurarMensaje) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página creada correctamente");
			Messages.keep();
			redirect("ConfigurarMensajesController.index", "editar", idConfigurarMensaje);
		}
		Messages.keep();
		redirect("RellenarMensajesController.index", "crear", idConfigurarMensaje);
	}

	@Util
	public static void borrarRender(Long idConfigurarMensaje) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página borrada correctamente");
			Messages.keep();
			redirect("ConfigurarMensajesController.index", "editar");
		}
		Messages.keep();
		redirect("RellenarMensajesController.index", "borrar", idConfigurarMensaje);
	}
	
	/*
	 * Método utilizado en RellenarMensajes.html. Permite que
	 * se pueda mostrar el contenido del mensaje correctamente.
	 */
	
	public static StringBuffer convertData(String contenido){
		return StringUtils.getParsedText(contenido);
	}
	
	
	//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// Métodos para comprobar qué servicio
	// no está disponible y mostrarlo mediante
	// una alerta al hacer el login.
	//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	
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
	
	public static List<ComboItem> paginaAconfigurar(){
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<ConfigurarMensaje> paginasMensaje = ConfigurarMensaje.findAll();
		for (ConfigurarMensaje lista: paginasMensaje){
			result.add(new ComboItem(lista.nombrePagina, lista.nombrePagina));
		}
		return result; 
	}
	
	public static void RellenarMensajesValidateCopy(String accion, ConfigurarMensaje dbConfigurarMensaje, ConfigurarMensaje configurarMensaje) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("paginaAConfigurar", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("configurarMensaje", configurarMensaje);
		}
		CustomValidation.valid("configurarMensaje", configurarMensaje);
		CustomValidation.required("configurarMensaje.tipoMensaje", configurarMensaje.tipoMensaje);
		CustomValidation.validValueFromTable("configurarMensaje.tipoMensaje", configurarMensaje.tipoMensaje);
		dbConfigurarMensaje.tipoMensaje = configurarMensaje.tipoMensaje;
		if (Arrays.asList(new String[] { "wiki" }).contains(dbConfigurarMensaje.tipoMensaje)) {
			CustomValidation.required("configurarMensaje.tituloMensaje", configurarMensaje.tituloMensaje);
			dbConfigurarMensaje.tituloMensaje = configurarMensaje.tituloMensaje;

		}
		CustomValidation.required("configurarMensaje.contenido", configurarMensaje.contenido);
		dbConfigurarMensaje.contenido = configurarMensaje.contenido;
		dbConfigurarMensaje.habilitar = configurarMensaje.habilitar;

	}
}
