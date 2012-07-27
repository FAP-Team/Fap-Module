package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import controllers.gen.IrVerificacionSinFinalizarControllerGen;

public class IrVerificacionSinFinalizarController extends IrVerificacionSinFinalizarControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/IrVerificacionSinFinalizar/IrVerificacionSinFinalizar.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion))
			solicitud = IrVerificacionSinFinalizarController.getSolicitudGenerica();
		else if (!"borrado".equals(accion))
			solicitud = IrVerificacionSinFinalizarController.getSolicitudGenerica(idSolicitud);

		redirect("PaginaVerificacionController.index", accion, idSolicitud, solicitud.verificacion.id);
	}

}
