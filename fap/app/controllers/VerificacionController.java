package controllers;

import java.util.ArrayList;
import java.util.List;

import play.Logger;

import tags.ComboItem;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import models.VerificacionDocumento;
import controllers.gen.VerificacionControllerGen;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class VerificacionController extends VerificacionControllerGen {

	public static void tablaverificacionDocumentos(Long idSolicitud,
			Long idEntidad) {

	}

	public static void iniciarVerificacion(Long idSolicitud, SolicitudGenerica solicitud) {
		checkAuthenticity();

		// Save code
		if (permisoiniciarVerificacion("update")
				|| permisoiniciarVerificacion("create")) {

			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);

			iniciarVerificacionValidateCopy(dbSolicitud, solicitud);

			if (!validation.hasErrors()) {
				iniciarVerificacionValidateRules(dbSolicitud, solicitud);
			}

			if (!validation.hasErrors()) {
				// Debemos recuperar todos los documentos aportados, del trámite seleccionado que no hayan sido verificados
				List<Documento> sinVerificar = solicitud.documentacion.getDocumentosNoVerificados();
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.verificandoTipos.name();
				
				dbSolicitud.save();
				Logger.info("Guardando solicitud " + dbSolicitud.id);
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		iniciarVerificacionRender(idSolicitud);

	}

}
