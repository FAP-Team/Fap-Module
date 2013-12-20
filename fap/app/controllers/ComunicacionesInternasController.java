package controllers;

import messages.Messages;
import models.ComunicacionInterna;
import models.SolicitudGenerica;
import play.mvc.Util;
import controllers.gen.ComunicacionesInternasControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class ComunicacionesInternasController extends ComunicacionesInternasControllerGen {
	@Util
	public static void formBotonRender(Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			ComunicacionInterna ci = new ComunicacionInterna();
			SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
			//Asocio la nueva comunicacion a la solicitud
			solicitud.comunicacionesInternas.add(ci);
			ci.estado = EstadosComunicacionInternaEnum.creada.name();
			ci.save();
			solicitud.save();
			redirect("paginaNuevaComunicacionDocumentosController.index", "editar", idSolicitud, ci.id);
		}
		Messages.keep();
		redirect("ComunicacionesInternasController.index", "editar", idSolicitud);
	}
	
	
}
