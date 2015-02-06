package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Agente;
import models.PeticionSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import services.SVDService;
import services.SVDServiceException;
import utils.SVDUtils;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.CesionDatosExpedienteSVDControllerGen;
import es.gobcan.platino.servicios.svd.Respuesta;

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

		renderJSON(response.toJSON("id", "datosGenericos.solicitante.nombreSolicitante", "datosGenericos.solicitante.unidadTramitadora", "datosGenericos.solicitante.idExpediente", "datosGenericos.solicitante.consentimiento", "fechaCreacion", "estado"));
	}

	public static void tablasolicitudesPeticionResidencia(Long idSolicitud) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP where nombreservicio=? and solicitud.id=?", "residencia", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, true, false, "adminOrGestor", "adminOrGestor", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "datosGenericos.solicitante.nombreSolicitante", "datosGenericos.solicitante.unidadTramitadora", "datosGenericos.solicitante.idExpediente", "datosGenericos.solicitante.consentimiento", "fechaCreacion", "estado"));
	}

	public static void enviarSolicitudesIdentidad(Long id, List<Long> idsSeleccionados) {
		//Sobreescribir para incorporar funcionalidad
		//No olvide asignar los permisos
		//index();

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);

		PeticionSVDFAP peticion = new PeticionSVDFAP();
		List<SolicitudTransmisionSVDFAP> listaSolicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();

		for (Long idSolicitudTransmision: idsSeleccionados) {
			SolicitudTransmisionSVDFAP solicitudTransmision = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP(idSolicitudTransmision);
			listaSolicitudesTransmision.add(solicitudTransmision);
		}

		svdService.crearPeticion(peticion, listaSolicitudesTransmision);

		try {
			Respuesta respuesta = svdService.enviarPeticionSincrona(peticion);
			if (!Messages.hasErrors()) {
				SVDUtils.respuestaSincronaPlatinoToRespuestaFAP(respuesta, peticion);
			}
		} catch (SVDServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CesionDatosExpedienteSVDController.editarRender(peticion.solicitudesTransmision.get(0).solicitud.id);

	}

	public static void enviarSolicitudesResidencia(Long id, List<Long> idsSeleccionados) {
		//Sobreescribir para incorporar funcionalidad
		//No olvide asignar los permisos
		//index();

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);

		PeticionSVDFAP peticion = new PeticionSVDFAP();
		List<SolicitudTransmisionSVDFAP> listaSolicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();

		for (Long idSolicitudTransmision: idsSeleccionados) {
			SolicitudTransmisionSVDFAP solicitudTransmision = EditarSolicitudTransmisionSVDResidenciaController.getSolicitudTransmisionSVDFAP(idSolicitudTransmision);
			listaSolicitudesTransmision.add(solicitudTransmision);
		}

		svdService.crearPeticion(peticion, listaSolicitudesTransmision);

		try {
			Respuesta respuesta = svdService.enviarPeticionSincrona(peticion);
			if (!Messages.hasErrors()) {
				SVDUtils.respuestaSincronaPlatinoToRespuestaFAP(respuesta, peticion);
			}
		} catch (SVDServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CesionDatosExpedienteSVDController.editarRender(peticion.solicitudesTransmision.get(0).solicitud.id);
	}

}
