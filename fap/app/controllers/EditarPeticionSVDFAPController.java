package controllers;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Agente;
import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;
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
		checkRedirigir();

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

		renderJSON(response.toJSON("id", "nombreServicio", "datosEspecificos.solicitud.id", "fechaCreacion", "estado", "respuesta.datosGenericos.transmision.fechaGeneracion", "descargarPDF"));
	}


	public static void tablatablaSolicitudesTransmisionResidencia(Long idPeticion) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from PeticionSVDFAP peticionSVDFAP join peticionSVDFAP.solicitudesTransmision solicitudTransmisionSVDFAP where peticionSVDFAP.id=?", idPeticion).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "nombreServicio", "datosEspecificos.solicitud.id", "fechaCreacion", "estado", "respuesta.datosGenericos.transmision.fechaGeneracion", "descargarPDF"));
	}
}
