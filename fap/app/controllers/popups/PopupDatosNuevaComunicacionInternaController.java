package controllers.popups;

import java.util.ArrayList;
import java.util.List;

import models.ComunicacionInterna;
import play.mvc.Util;
import properties.FapProperties;
import tags.ComboItem;
import utils.ComunicacionesInternasUtils;
import validation.CustomValidation;
import controllers.gen.popups.PopupDatosNuevaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PopupDatosNuevaComunicacionInternaController extends PopupDatosNuevaComunicacionInternaControllerGen {

	@Util
	public static void PopupDatosNuevaComunicacionInternaValidateCopy(String accion, ComunicacionInterna dbComunicacionInterna, ComunicacionInterna comunicacionInterna) {
		CustomValidation.clearValidadas();

		CustomValidation.valid("comunicacionInterna.asiento", comunicacionInterna.asiento);
		CustomValidation.valid("comunicacionInterna", comunicacionInterna);
		CustomValidation.required("comunicacionInterna.asiento.userId", comunicacionInterna.asiento.userId);
		dbComunicacionInterna.asiento.userId = comunicacionInterna.asiento.userId;
		CustomValidation.required("comunicacionInterna.asiento.password", comunicacionInterna.asiento.password);
		dbComunicacionInterna.asiento.password = comunicacionInterna.asiento.password;
		CustomValidation.required("comunicacionInterna.asiento.interesado", comunicacionInterna.asiento.interesado);
		dbComunicacionInterna.asiento.interesado = comunicacionInterna.asiento.interesado;
		CustomValidation.valid("comunicacionInterna.asiento.unidadOrganicaDestino", comunicacionInterna.asiento.unidadOrganicaDestino);
		CustomValidation.required("comunicacionInterna.asiento.unidadOrganicaDestino.codigo", comunicacionInterna.asiento.unidadOrganicaDestino.codigo);
		CustomValidation.validValueFromTable("comunicacionInterna.asiento.unidadOrganicaDestino.codigo", comunicacionInterna.asiento.unidadOrganicaDestino.codigo);
		dbComunicacionInterna.asiento.unidadOrganicaDestino.codigo = comunicacionInterna.asiento.unidadOrganicaDestino.codigo;
		CustomValidation.required("comunicacionInterna.asiento.resumen", comunicacionInterna.asiento.resumen);
		dbComunicacionInterna.asiento.resumen = comunicacionInterna.asiento.resumen;
		dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.creada.name();

	}
	
	public static List<ComboItem> uoDestino() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		result = ComunicacionesInternasUtils.unidadesOrganicasCombo(true, false);
		return result;
	}
}
