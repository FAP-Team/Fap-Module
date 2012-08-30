package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import controllers.gen.IrPresentacionPendienteFHControllerGen;

public class IrPresentacionPendienteFHController extends IrPresentacionPendienteFHControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/IrPresentacionPendienteFH/IrPresentacionPendienteFH.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = IrPresentacionPendienteFHController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = IrPresentacionPendienteFHController.getSolicitudGenerica(idSolicitud);

		Long idRegistro = solicitud.registro.id;
		redirect("SolicitudPresentarFAPController.index", accion, idSolicitud, idRegistro);
	}
	
}
