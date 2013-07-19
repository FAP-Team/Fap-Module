package controllers.popups;

import messages.Messages;
import messages.Messages.MessageType;
import models.Documento;
import play.mvc.Util;
import controllers.gen.popups.PopUpAportacionesControllerGen;

public class PopUpAportacionesController extends PopUpAportacionesControllerGen {

	@Util
	public static Documento getDocumento(Long idSolicitud, Long idDocumento) {
		Documento documento = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}

		if (idDocumento == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idDocumento"))
				Messages.fatal("Falta parámetro idDocumento");
		}
		if (idSolicitud != null && idDocumento != null) {
			documento = Documento.find("select registradas.registro.justificante from Solicitud solicitud " +
						  "join solicitud.aportaciones.registradas registradas " +
						  "where solicitud.id=? and registradas.registro.justificante.id=?", idSolicitud, idDocumento).first();
			if (documento == null)
				Messages.fatal("Error al recuperar Documento");
		}
		return documento;
	}
	
}
