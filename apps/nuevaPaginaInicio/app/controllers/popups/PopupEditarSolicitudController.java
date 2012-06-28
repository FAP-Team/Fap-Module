package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.ExpedienteGenerico;
import models.Solicitud;
import tags.ComboItem;
import controllers.gen.popups.PopupEditarSolicitudControllerGen;
import messages.Messages;
import messages.Messages.MessageType;

public class PopupEditarSolicitudController extends PopupEditarSolicitudControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("nuevaPaginaInicio/popups/PopupEditarSolicitud.html");
		}

		Solicitud solicitud = null;
		if ("crear".equals(accion))
			solicitud = PopupEditarSolicitudController.getSolicitud();
		else if (!"borrado".equals(accion))
			solicitud = PopupEditarSolicitudController.getSolicitud(idSolicitud);

		log.info("Visitando página: " + "gen/popups/PopupEditarSolicitud.html");
		renderTemplate("nuevaPaginaInicio/popups/PopupEditarSolicitud.html", accion, idSolicitud, solicitud);
	}
	
	
	public static List<ComboItem> expediente() {
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Long idSolicitud = ids.get("idSolicitud");
		if (idSolicitud == null) {
			Messages.fatal("Falta parámetro idSolicitud");
			return null;
		}

		ExpedienteGenerico expediente = ExpedienteGenerico.find("select expediente " +
																"from ExpedienteGenerico expediente join expediente.solicitud solicitud " +
																"where solicitud.id = " + idSolicitud).first();
		List<ComboItem> result = new ArrayList<ComboItem>();
		result.add( new ComboItem(expediente.id, expediente.idExpediente) );
		return result;

	}
}
