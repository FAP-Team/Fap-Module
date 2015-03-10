package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import play.mvc.Util;
import properties.FapProperties;
import services.FirmaService;
import services.platino.PlatinoGestorDocumentalService;
import utils.ComunicacionesInternasUtils;
import utils.ServiciosGenericosUtils;
import messages.Messages;
import models.Agente;
import models.ReturnUnidadOrganicaFap;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.UnidadesOrganicasControllerGen;
import services.ServiciosGenericosService;

public class UnidadesOrganicasController extends UnidadesOrganicasControllerGen {

	@Inject
	protected static ServiciosGenericosService genericosService;
	
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmUO(String btnCargarUO) {
		checkAuthenticity();
		if (!permisoFrmUO("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			UnidadesOrganicasController.frmUOValidateRules();
		}
		
		String usuarioHiperReg = FapProperties.get("fap.platino.registro.username");
		String passwordHiperReg = FapProperties.get("fap.platino.registro.password");
		
		if (usuarioHiperReg == null || "undefined".equals(usuarioHiperReg) || passwordHiperReg == null || "undefined".equals(passwordHiperReg)) {
			log.info("No se han definido las credenciales del usuario HiperReg, consultar properties");
			Messages.error("Faltan las credenciales del usuario HiperReg");
			Messages.keep();
		}
		
		if (!Messages.hasErrors()){
			Long codigoSuperior = new Long(0); //Código que indica las UO de nivel superior
			List<ReturnUnidadOrganicaFap> lstUO = genericosService.obtenerUnidadesOrganicas(codigoSuperior,	usuarioHiperReg, passwordHiperReg);
			
			List<ReturnUnidadOrganicaFap> lstUOB = new ArrayList<ReturnUnidadOrganicaFap>();
			List<ReturnUnidadOrganicaFap> lstUOSlave = new ArrayList<ReturnUnidadOrganicaFap>();
			for (ReturnUnidadOrganicaFap uo: lstUO){
				
				if (uo.codigo != null && uo.error.codigo == 0){
					lstUOSlave = genericosService.obtenerUnidadesOrganicas(uo.codigo, usuarioHiperReg, passwordHiperReg);
					
					if (lstUOSlave != null)
						lstUOB.addAll(lstUOSlave);
				}
				
			}
			
			lstUO.addAll(lstUOB);
			ServiciosGenericosUtils.cargarUnidadesOrganicas(lstUO);
		}
		
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		UnidadesOrganicasController.frmUORender();
	}
	
}
