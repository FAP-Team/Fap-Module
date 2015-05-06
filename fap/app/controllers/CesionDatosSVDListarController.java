package controllers;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.PeticionSVDFAP;
import play.mvc.Util;
import services.SVDService;
import services.SVDServiceException;
import config.InjectorConfig;
import controllers.gen.CesionDatosSVDListarControllerGen;

public class CesionDatosSVDListarController extends CesionDatosSVDListarControllerGen {

	public static void tablatablaCesionesIdentidad() {

		java.util.List<PeticionSVDFAP> rows = PeticionSVDFAP.find("select peticionSVDFAP from PeticionSVDFAP peticionSVDFAP where peticionSVDFAP.nombreServicio=?", "identidad").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<PeticionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<PeticionSVDFAP> response = new tables.TableRenderResponse<PeticionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "estadoPeticion"));
	}

	public static void enviarpeticionesIdentidad(Long id, List<Long> idsSeleccionados) {

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);

		try {
			for (Long idPeticion: idsSeleccionados) {
				PeticionSVDFAP peticion = PeticionSVDFAP.findById(idPeticion);
				try {
					svdService.enviarPeticionAsincrona(peticion);
				} catch (SVDServiceException e) {
					Messages.error("Error al enviar la petición asíncrona");
					play.Logger.error("Error al enviar la petición asíncrona: " + e);
					e.printStackTrace();
				}
			}
			if (!Messages.hasErrors()) {
				Messages.ok("Peticiones enviadas con éxito");
				play.Logger.info("Peticiones enviadas con éxito");
			}
		} catch (Exception ex) {
			Messages.error("Se ha producido algún error enviando las peticiones seleccionadas...");
			play.Logger.error("Error al enviar alguna petición: " + ex);
		}

		CesionDatosSVDListarController.editarRender();

	}

	public static void tablatablaCesionesResidencia() {

		java.util.List<PeticionSVDFAP> rows = PeticionSVDFAP.find("select peticionSVDFAP from PeticionSVDFAP peticionSVDFAP where peticionSVDFAP.nombreServicio=?", "residencia").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<PeticionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<PeticionSVDFAP> response = new tables.TableRenderResponse<PeticionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "estadoPeticion"));
	}

	public static void enviarpeticionesResidencia(Long id, List<Long> idsSeleccionados) {

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);

		try {
			for (Long idPeticion: idsSeleccionados) {
				PeticionSVDFAP peticion = PeticionSVDFAP.findById(idPeticion);
				try {
					svdService.enviarPeticionAsincrona(peticion);
				} catch (SVDServiceException e) {
					Messages.error("Error al enviar la petición asíncrona");
					play.Logger.error("Error al enviar la petición asíncrona: " + e);
					e.printStackTrace();
				}
			}
			if (!Messages.hasErrors()) {
				Messages.ok("Peticiones enviadas con éxito");
				play.Logger.info("Peticiones enviadas con éxito");
			}
		} catch (Exception ex) {
			Messages.error("Se ha producido algún error enviando las peticiones seleccionadas...");
			play.Logger.error("Error al enviar alguna petición: " + ex);
		}

		CesionDatosSVDListarController.editarRender();
	}

	@Util
	public static void editarRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("CesionDatosSVDListarController.index", "editar");
		}
		Messages.keep();
		redirect("CesionDatosSVDListarController.index", "editar");
	}

}
