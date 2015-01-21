package controllers;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import controllers.fap.AgenteController;
import controllers.gen.CesionDatosExpedienteSVDControllerGen;

public class CesionDatosExpedienteSVDController extends CesionDatosExpedienteSVDControllerGen {

	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/CesionDatosExpedienteSVD/CesionDatosExpedienteSVD.html");
		}
		checkRedirigir();

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = CesionDatosExpedienteSVDController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = CesionDatosExpedienteSVDController.getSolicitudGenerica(idSolicitud);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/CesionDatosExpedienteSVD/CesionDatosExpedienteSVD.html" + " Agente: " + logAgente);
		renderTemplate("fap/CesionDatosExpedienteSVD/CesionDatosExpedienteSVD.html", accion, idSolicitud, solicitud);
	}

	public static void tablasolicitudesPeticionIdentidad(Long idSolicitud) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP where nombreservicio=? and solicitud.id=?", "identidad", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, true, false, "adminOrGestor", "adminOrGestor", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "fechaCreacion", "estado", "datosGenericos.solicitante.funcionario.nombreCompletoFuncionario", "datosGenericos.solicitante.unidadTramitadora", "datosGenericos.solicitante.consentimiento"));
	}

	public static void tablasolicitudesPeticionResidencia(Long idSolicitud) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP where nombreservicio=? and solicitud.id=?", "residencia", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, true, false, "adminOrGestor", "adminOrGestor", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "fechaCreacion", "estado", "datosGenericos.solicitante.funcionario.nombreCompletoFuncionario", "datosGenericos.solicitante.unidadTramitadora", "datosGenericos.solicitante.consentimiento"));
	}

}
