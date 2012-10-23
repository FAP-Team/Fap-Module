package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import play.mvc.Util;
import controllers.gen.ActivarFuncionarioHabilitadoControllerGen;

public class ActivarFuncionarioHabilitadoController extends ActivarFuncionarioHabilitadoControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/ActivarFuncionarioHabilitado/ActivarFuncionarioHabilitado.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = ActivarFuncionarioHabilitadoController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = ActivarFuncionarioHabilitadoController.getSolicitudGenerica(idSolicitud);

		log.info("Visitando página: " + "fap/ActivarFuncionarioHabilitado/ActivarFuncionarioHabilitado.html");
		renderTemplate("fap/ActivarFuncionarioHabilitado/ActivarFuncionarioHabilitado.html", accion, idSolicitud, solicitud);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formActivarFH(Long idSolicitud, String activarFH) {
		checkAuthenticity();
		if (!permisoFormActivarFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {	
			solicitud.activoFH=true;
		}

		if (!Messages.hasErrors()) {
			ActivarFuncionarioHabilitadoController.formActivarFHValidateRules();
		}
		if (!Messages.hasErrors()) {
			solicitud.save();
			log.info("Activación de Funcionario Habilitado, llevada a cabo con éxito en solicitud: "+idSolicitud);
			Messages.ok("Se ha activado correctamente la posibilidad de que el solicitante de esta solicitud pueda requerir la firma de un Funcionario Publico");
		} else
			log.info("Acción Editar de página: " + "gen/ActivarFuncionarioHabilitado/ActivarFuncionarioHabilitado.html" + " , intentada sin éxito (Problemas de Validación)");
		ActivarFuncionarioHabilitadoController.formActivarFHRender(idSolicitud);
	}
	
}
