package controllers;

import messages.Messages;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import play.mvc.Util;
import resolucion.ResolucionBase;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaNotificarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;

public class PaginaNotificarResolucionController extends PaginaNotificarResolucionControllerGen {
	
	public static void index(String accion, Long idResolucionFAP) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html");
		}

		ResolucionFAP resolucionFAP = null;
		if ("crear".equals(accion)) {
			resolucionFAP = PaginaNotificarResolucionController.getResolucionFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				resolucionFAP.save();
				idResolucionFAP = resolucionFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			resolucionFAP = PaginaNotificarResolucionController.getResolucionFAP(idResolucionFAP);

		log.info("Visitando página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html", accion, idResolucionFAP, resolucionFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formGenerarOficioRemision(Long idResolucionFAP, String botonGenerarOficioRemision) {
		checkAuthenticity();
		if (!permisoFormGenerarOficioRemision("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formGenerarOficioRemisionValidateRules();
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolBase.generarOficioRemision(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		} else {
			play.Logger.info("No se genero el documento de oficio de remision para la resolucion "+idResolucionFAP);
		}

		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formGenerarOficioRemisionRender(idResolucionFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void notificar(Long idResolucionFAP, int fapNotificacionPlazoacceso, int fapNotificacionFrecuenciarecordatorioacceso, int fapNotificacionPlazorespuesta, int fapNotificacionFrecuenciarecordatoriorespuesta) {

		ResolucionBase resolBase = null;
		try {
			resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			resolBase.notificarCopiarEnExpedientes(idResolucionFAP, fapNotificacionPlazoacceso, fapNotificacionFrecuenciarecordatorioacceso, fapNotificacionPlazorespuesta, fapNotificacionFrecuenciarecordatoriorespuesta);
			resolBase.resolucion.estadoNotificacion = EstadoResolucionEnum.notificada.name();
			resolBase.resolucion.save();
		} catch (Throwable e) {
			new Exception ("No se ha podido obtener el objeto resolución", e);
		}

		if (!Messages.hasErrors()) {
			log.info("Notificación con éxito");
			redirect("EditarResolucionController.index", EditarResolucionController.getAccion(), idResolucionFAP);
		} else {
			log.info("Notificación sin éxito");
			Messages.keep();
			redirect("PaginaNotificarResolucionController.index", "editar", idResolucionFAP);
		}
	}

	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formVolver(Long idResolucionFAP) {
		checkAuthenticity();
		if (!permisoFormVolver("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formVolverValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formVolverRender(idResolucionFAP);
	}
	
}
