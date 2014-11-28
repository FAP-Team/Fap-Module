package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import play.mvc.Util;
import properties.FapProperties;
import services.ComunicacionesInternasServiceException;
import services.FirmaService;
import services.comunicacionesInternas.ComunicacionesInternasServiceImpl;
import services.platino.PlatinoGestorDocumentalService;
import utils.ComunicacionesInternasUtils;
import messages.Messages;
import models.Agente;
import models.ReturnUnidadOrganicaFap;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.UnidadesOrganicasControllerGen;

public class UnidadesOrganicasController extends UnidadesOrganicasControllerGen {

	@Inject
	protected static ComunicacionesInternasServiceImpl ciService;
	
	
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
		
		if (!Messages.hasErrors()){
			try {
				Long codigoSuperior = new Long(0); //Código que indica las UO de nivel superior
				List<ReturnUnidadOrganicaFap> lstUO = ciService.obtenerUnidadesOrganicas(codigoSuperior);
				List<ReturnUnidadOrganicaFap> lstUOB = new ArrayList<ReturnUnidadOrganicaFap>();
				List<ReturnUnidadOrganicaFap> lstUOSlave = new ArrayList<ReturnUnidadOrganicaFap>();
				
				for (ReturnUnidadOrganicaFap uo: lstUO){
					if (uo.codigo != null && uo.error.codigo == 0){
						lstUOSlave = ciService.obtenerUnidadesOrganicas(uo.codigo);
						
						if (lstUOSlave != null)
							lstUOB.addAll(lstUOSlave);
					}
				}
				
				lstUO.addAll(lstUOB);
				ComunicacionesInternasUtils.cargarUnidadesOrganicas(lstUO);
			} catch (ComunicacionesInternasServiceException e) {
				log.error("No se han podido recuperar las Unidades Orgánicas");
				Messages.error("No se han podido recuperar las Unidades Orgánicas");
			}
		}
		
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		UnidadesOrganicasController.frmUORender();
	}
	
}
