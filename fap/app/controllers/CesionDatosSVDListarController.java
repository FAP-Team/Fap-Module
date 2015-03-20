package controllers;

import java.util.List;
import java.util.Map;

import models.PeticionSVDFAP;
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
		//Sobreescribir para incorporar funcionalidad
		//No olvide asignar los permisos
		//index();
	}

	public static void tablatablaCesionesResidencia() {

		java.util.List<PeticionSVDFAP> rows = PeticionSVDFAP.find("select peticionSVDFAP from PeticionSVDFAP peticionSVDFAP where peticionSVDFAP.nombreServicio=?", "residencia").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<PeticionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<PeticionSVDFAP> response = new tables.TableRenderResponse<PeticionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "estadoPeticion"));
	}

	public static void enviarpeticionesResidencia(Long id, List<Long> idsSeleccionados) {
		//Sobreescribir para incorporar funcionalidad
		//No olvide asignar los permisos
		//index();
	}

}
