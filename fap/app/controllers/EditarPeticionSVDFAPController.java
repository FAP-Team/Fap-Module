package controllers;

import java.util.List;
import java.util.Map;

import services.verificacionDatos.SVDService;
import messages.Messages;
import models.Agente;
import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.EditarPeticionSVDFAPControllerGen;

public class EditarPeticionSVDFAPController extends EditarPeticionSVDFAPControllerGen {

	public static void index(String accion, Long idPeticionSVDFAP) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EditarPeticionSVDFAP/EditarPeticionSVDFAP.html");
		}

		PeticionSVDFAP peticionSVDFAP = null;
		if ("crear".equals(accion)) {
			peticionSVDFAP = EditarPeticionSVDFAPController.getPeticionSVDFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				peticionSVDFAP.save();
				idPeticionSVDFAP = peticionSVDFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			peticionSVDFAP = EditarPeticionSVDFAPController.getPeticionSVDFAP(idPeticionSVDFAP);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/EditarPeticionSVDFAP/EditarPeticionSVDFAP.html" + " Agente: " + logAgente);
		renderTemplate("fap/EditarPeticionSVDFAP/EditarPeticionSVDFAP.html", accion, idPeticionSVDFAP, peticionSVDFAP);
	}


	public static void tablatablaSolicitudesTransmisionIdentidad(Long idPeticion) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from PeticionSVDFAP peticionSVDFAP join peticionSVDFAP.solicitudesTransmision solicitudTransmisionSVDFAP where peticionSVDFAP.id=?", idPeticion).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "nombreServicio", "datosGenericos.solicitante.idExpediente", "fechaCreacion", "estado", "fechaRespuesta", "justificanteSVD.enlaceDescarga"));
	}


	public static void tablatablaSolicitudesTransmisionResidencia(Long idPeticion) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from PeticionSVDFAP peticionSVDFAP join peticionSVDFAP.solicitudesTransmision solicitudTransmisionSVDFAP where peticionSVDFAP.id=?", idPeticion).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "nombreServicio", "datosGenericos.solicitante.idExpediente", "fechaCreacion", "estado", "fechaRespuesta", "justificanteSVD.enlaceDescarga"));
	}


	//Prepara una petición ya creada para poder enviarla
	public static void prepararPeticion(Long idPeticionSVDFAP) {

		PeticionSVDFAP peticionSVDFAP = EditarPeticionSVDFAPController.getPeticionSVDFAP(idPeticionSVDFAP);

		try {
			for (SolicitudTransmisionSVDFAP solicitudTransmision : peticionSVDFAP.solicitudesTransmision) {
				solicitudTransmision.estado = "preparada";
				solicitudTransmision.save();
			}
			peticionSVDFAP.estadoPeticion = "preparada";
			peticionSVDFAP.save();
			play.Logger.info("Solicitudes de transmisión y petición preparadas para su envío");
		} catch (Exception ex) {
			Messages.error("Una o más solicitudes de transmisión no han podido ser preparadas");
		}

		redirect("CesionDatosSVDListarController.index", "editar");
	}


	//Solicita Respuesta de una petición Asíncrona
	public static void solicitarRespuesta(Long idPeticionSVDFAP) {
		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);
		PeticionSVDFAP peticionSVDFAP = null;

		try {
			peticionSVDFAP = EditarPeticionSVDFAPController.getPeticionSVDFAP(idPeticionSVDFAP);
			svdService.solicitudRespuesta(peticionSVDFAP);
			peticionSVDFAP.save();
		} catch (Exception ex) {
			play.Logger.error("Se ha producido un error realizando la solicitud de respuesta asíncrona");
			Messages.error("Error solicitando respuesta");
		}
	}
}
