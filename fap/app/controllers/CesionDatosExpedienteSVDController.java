package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import services.verificacionDatos.SVDService;
import services.verificacionDatos.SVDServiceException;
import services.verificacionDatos.SVDUtils;
import messages.Messages;
import models.Agente;
import models.PeticionSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.CesionDatosExpedienteSVDControllerGen;
import enumerado.fap.gen.NombreServicioSVDFAPEnum;

public class CesionDatosExpedienteSVDController extends CesionDatosExpedienteSVDControllerGen {

	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/CesionDatosExpedienteSVD/CesionDatosExpedienteSVD.html");
		}

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

		renderJSON(response.toJSON("datosGenericos.transmision.idTransmision", "datosGenericos.solicitante.idExpediente", "fechaCreacion", "fechaRespuesta", "justificanteSVD.enlaceDescarga", "estado", "id"));
	}

	public static void tablasolicitudesPeticionResidencia(Long idSolicitud) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP where nombreservicio=? and solicitud.id=?", "residencia", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, true, false, "adminOrGestor", "adminOrGestor", "", getAccion(), ids);

		renderJSON(response.toJSON("datosGenericos.transmision.idTransmision", "datosGenericos.solicitante.idExpediente", "fechaCreacion", "fechaRespuesta", "justificanteSVD.enlaceDescarga", "estado", "id"));
	}

	public static void enviarSolicitudesIdentidad(Long id, List<Long> idsSeleccionados) {
		PeticionSVDFAP peticion = null;
		List<SolicitudTransmisionSVDFAP> listaSolicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();
		SolicitudTransmisionSVDFAP solicitudTransmision = null;
		
		try {
			SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);
			
			if (idsSeleccionados.size() == 1){
				peticion = new PeticionSVDFAP();
				solicitudTransmision = SVDUtils.getSolicitudTransmisionSVDFAP(idsSeleccionados.get(0));
				listaSolicitudesTransmision.add(solicitudTransmision);
				SVDUtils.crearPeticion(peticion, listaSolicitudesTransmision, NombreServicioSVDFAPEnum.identidad.name());
				svdService.peticionSincrona(peticion);
			} else
				Messages.info("Sólo se permite una consulta de datos síncrona cada vez");
			
		} catch (SVDServiceException e) {
			Messages.error("Error al enviar la petición síncrona");
			play.Logger.error("Error al enviar la petición síncrona: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception er) {
			Messages.error("Error al enviar la petición síncrona");
			play.Logger.error("Error al enviar la petición síncrona: " + er.getMessage());
			er.printStackTrace();
		}
		
		if (!Messages.hasErrors()) {
			peticion.save();
			Messages.ok("Respuesta recibida con éxito");
			play.Logger.info("Petición enviada con éxito");
			VerCesionesDatosIdentidadExpedienteSVDController.index("leer", solicitudTransmision.getId(), id);
		}
		else
		   CesionDatosExpedienteSVDController.index(getAccion(), id);
	}

	public static void enviarSolicitudesResidencia(Long id, List<Long> idsSeleccionados) {
		PeticionSVDFAP peticion = null;
		List<SolicitudTransmisionSVDFAP> listaSolicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();
		SolicitudTransmisionSVDFAP solicitudTransmision = null;
		
		try {
			SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);

			if (idsSeleccionados.size() == 1){
				peticion = new PeticionSVDFAP();
				solicitudTransmision = SVDUtils.getSolicitudTransmisionSVDFAP(idsSeleccionados.get(0));
				listaSolicitudesTransmision.add(solicitudTransmision);
				SVDUtils.crearPeticion(peticion, listaSolicitudesTransmision, NombreServicioSVDFAPEnum.residencia.name());
				svdService.peticionSincrona(peticion);
			} else
				Messages.info("Sólo se permite una consulta de datos síncrona cada vez");
			
			if (!Messages.hasErrors()) {
				Messages.ok("Respuesta recibida con éxito");
				play.Logger.info("Petición enviada con éxito");

			}
		} catch (SVDServiceException e) {
			Messages.keep();
			Messages.error("Error al enviar la petición síncrona");
			play.Logger.error("Error al enviar la petición síncrona: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception er) {
			Messages.keep();
			Messages.error("Error al enviar la petición síncrona");
			play.Logger.error("Error al enviar la petición síncrona: " + er.getMessage());
			er.printStackTrace();
		}
		
		if (!Messages.hasErrors()) {
			peticion.save();
			Messages.keep();
			Messages.ok("Respuesta recibida con éxito");
			play.Logger.info("Petición enviada con éxito");
			VerCesionesDatosIdentidadExpedienteSVDController.index("leer", solicitudTransmision.getId(), id);
		}
		else
		   CesionDatosExpedienteSVDController.index(getAccion(), id);
	}

	public static void crearIdentidad(Long idSolicitud) {
		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		try {
			solicitudTransmisionSVDFAP = SVDUtils.crearSolicitudTransmisionSVDFAP(NombreServicioSVDFAPEnum.identidad.name(), idSolicitud);
		} catch (SVDServiceException e) {
			play.Logger.error("Error al crear la solicitud de transmisión de datos de identidad: " + e.getMessage());
		}
		
		if (!Messages.hasErrors()) {
			solicitudTransmisionSVDFAP.save();
			EditarCesionesDatosIdentidadExpedienteSVDController.editarRender(solicitudTransmisionSVDFAP.getId(), idSolicitud);
		}else
			CesionDatosExpedienteSVDController.index("crear", idSolicitud);
	}

	public static void crearResidencia(Long idSolicitud) {
		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		try {
			solicitudTransmisionSVDFAP = SVDUtils.crearSolicitudTransmisionSVDFAP(NombreServicioSVDFAPEnum.residencia.name(), idSolicitud);
		} catch (SVDServiceException e) {
			play.Logger.error("Error al crear la solicitud de transmisión de datos de residencia: " + e.getMessage());
		}
		
		if (!Messages.hasErrors()) {
			solicitudTransmisionSVDFAP.save();
			EditarCesionesDatosIdentidadExpedienteSVDController.editarRender(solicitudTransmisionSVDFAP.getId(), idSolicitud);
		}else
			CesionDatosExpedienteSVDController.index("crear", idSolicitud);
	}

}
