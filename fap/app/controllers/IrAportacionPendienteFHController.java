package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import controllers.gen.IrAportacionPendienteFHControllerGen;

public class IrAportacionPendienteFHController extends IrAportacionPendienteFHControllerGen {

	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/IrAportacionPendienteFH/IrAportacionPendienteFH.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = IrAportacionPendienteFHController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = IrAportacionPendienteFHController.getSolicitudGenerica(idSolicitud);

		redirect("AportacionPresentarController.index", accion, idSolicitud);
	}
	
}
