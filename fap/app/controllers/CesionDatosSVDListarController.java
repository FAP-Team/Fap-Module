package controllers;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import models.Agente;
import models.ComboNombreServicioSVDFAP;
import models.PeticionSVDFAP;
import play.mvc.Util;
import services.verificacionDatos.SVDService;
import services.verificacionDatos.SVDServiceException;
import services.verificacionDatos.SVDUtils;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.CesionDatosSVDListarControllerGen;

public class CesionDatosSVDListarController extends CesionDatosSVDListarControllerGen {
	
	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/CesionDatosSVDListar/CesionDatosSVDListar.html");
		}

		ComboNombreServicioSVDFAP comboNombreServicioSVDFAP = CesionDatosSVDListarController.getComboNombreServicioSVDFAP();
		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/CesionDatosSVDListar/CesionDatosSVDListar.html" + " Agente: " + logAgente);
		renderTemplate("fap/CesionDatosSVDListar/CesionDatosSVDListar.html", accion, comboNombreServicioSVDFAP);
	}

	public static void tablatablaCesionesIdentidad() {

		java.util.List<PeticionSVDFAP> rows = PeticionSVDFAP.find("select peticionSVDFAP from PeticionSVDFAP peticionSVDFAP where peticionSVDFAP.nombreServicio=?", "identidad").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<PeticionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<PeticionSVDFAP> response = new tables.TableRenderResponse<PeticionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "fechaCreacion", "fechaRespuesta", "atributos.idPeticion", "estadoPeticion"));
	}

	public static void enviarpeticionesIdentidad(Long id, List<Long> idsSeleccionados) {

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);
		PeticionSVDFAP peticion = null;
		
		try {
			for (Long idPeticion: idsSeleccionados) {
				try {
					peticion = PeticionSVDFAP.findById(idPeticion);
					svdService.peticionAsincrona(peticion);
					peticion.save();
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

		renderJSON(response.toJSON("id", "fechaCreacion", "fechaRespuesta", "atributos.idPeticion", "estadoPeticion"));
	}

	public static void enviarpeticionesResidencia(Long id, List<Long> idsSeleccionados) {

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);
		PeticionSVDFAP peticion = null;

		try {
			for (Long idPeticion: idsSeleccionados) {
				try {
					peticion = PeticionSVDFAP.findById(idPeticion);
					svdService.peticionAsincrona(peticion);
					peticion.save();
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
