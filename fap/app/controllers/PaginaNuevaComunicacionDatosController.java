package controllers;

import java.util.ArrayList;
import java.util.List;

import models.ComunicacionInterna;
import models.ReturnUnidadOrganicaFap;
import play.mvc.Util;
import properties.FapProperties;

import tags.ComboItem;
import utils.ComunicacionesInternasUtils;
import validation.CustomValidation;
import controllers.gen.PaginaNuevaComunicacionDatosControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PaginaNuevaComunicacionDatosController extends PaginaNuevaComunicacionDatosControllerGen {

	@Util
	public static void PaginaNuevaComunicacionDatosValidateCopy(String accion, ComunicacionInterna dbComunicacionInterna, ComunicacionInterna comunicacionInterna) {
		CustomValidation.clearValidadas();

		CustomValidation.valid("comunicacionInterna.asiento", comunicacionInterna.asiento);
		CustomValidation.valid("comunicacionInterna", comunicacionInterna);
		CustomValidation.required("comunicacionInterna.asiento.observaciones", comunicacionInterna.asiento.observaciones);
		dbComunicacionInterna.asiento.observaciones = comunicacionInterna.asiento.observaciones;
		CustomValidation.required("comunicacionInterna.asiento.resumen", comunicacionInterna.asiento.resumen);
		dbComunicacionInterna.asiento.resumen = comunicacionInterna.asiento.resumen;
		dbComunicacionInterna.asiento.interesado = comunicacionInterna.asiento.interesado;
		CustomValidation.validValueFromTable("comunicacionInterna.asiento.unidadOrganicaDestino.codigo", comunicacionInterna.asiento.unidadOrganicaDestino.codigo);
		//ComunicacionInterna comunicacionInterna = ComunicacionInterna.find("Select comunicacionInterna from ComunicacionInterna where comunicacionesInternas.codigo = ?", comunicacionInterna.asiento.unidadOrganicaDestino.codigo;).first();
		ReturnUnidadOrganicaFap unidadOrganica = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", comunicacionInterna.asiento.unidadOrganicaDestino.codigo).first();
		dbComunicacionInterna.asiento.unidadOrganicaDestino = unidadOrganica;
		dbComunicacionInterna.asiento.userId = comunicacionInterna.asiento.userId;
		dbComunicacionInterna.asiento.password = comunicacionInterna.asiento.password;
		//Todo válido, actualizo estado de la comunicacion
		comunicacionInterna.estado = EstadosComunicacionInternaEnum.datosCompletos.name();
		
	}
	
	public static List<ComboItem> uoDestino() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		//Falta el user y el pass
		String userId = FapProperties.get("fap.platino.registro.username");
		String password = FapProperties.get("fap.platino.registro.password");
		result = ComunicacionesInternasUtils.unidadesOrganicas2Combo(userId, password);
		return result;
	}
	
}
