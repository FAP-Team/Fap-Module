package controllers;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gson.reflect.TypeToken;

import play.Play;
import play.mvc.Util;
import properties.FapProperties;
import services.FirmaService;
import services.platino.PlatinoGestorDocumentalService;
import utils.ComunicacionesInternasUtils;
import utils.JsonUtils;
import utils.ServiciosGenericosUtils;
import messages.Messages;
import models.Agente;
import models.MapeoUOBDOrganizacionHiperreg;
import models.ReturnUnidadOrganicaFap;
import models.TipoCEconomico;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.UnidadesOrganicasControllerGen;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;
import services.ServiciosGenericosService;

public class UnidadesOrganicasController extends UnidadesOrganicasControllerGen {

	@Inject
	protected static ServiciosGenericosService genericosService;
	
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmCargarUOServicio(String btnCargarUOServicio) {
		checkAuthenticity();
		if (!permisoFrmCargarUOServicio("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			try {
				btnCargarUOServicioFrmCargarUO();
			} catch (InterruptedException e) {
				log.error("Ha ocurrido un error en la carga de Unidades Orgánicas desde Servicio: "+e.getMessage());
			}
		}

		if (!Messages.hasErrors()) {
			UnidadesOrganicasController.frmCargarUOServicioValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		UnidadesOrganicasController.frmCargarUOServicioRender();
	}
	
	@Util
	public static void btnCargarUOServicioFrmCargarUO() throws InterruptedException {
		String usuarioHiperReg = FapProperties.get("fap.platino.registro.username");
		String passwordHiperReg = FapProperties.get("fap.platino.registro.password");
		
		if (usuarioHiperReg == null || "undefined".equals(usuarioHiperReg) || passwordHiperReg == null || "undefined".equals(passwordHiperReg)) {
			log.info("No se han definido las credenciales del usuario HiperReg, consultar properties");
			Messages.error("Faltan las credenciales del usuario de HiperReg");
			Messages.keep();
		}
		
		if (!Messages.hasErrors()){
			try{
				Long codigoSuperior = new Long(0); //Código que indica las UO de nivel superior
				List<ReturnUnidadOrganicaFap> lstUO = genericosService.obtenerUnidadesOrganicas(codigoSuperior,	usuarioHiperReg, passwordHiperReg);
				
				List<ReturnUnidadOrganicaFap> lstUOB = new ArrayList<ReturnUnidadOrganicaFap>();
				List<ReturnUnidadOrganicaFap> lstUOSlave = new ArrayList<ReturnUnidadOrganicaFap>();
				for (ReturnUnidadOrganicaFap uo: lstUO){
					
					if (uo.codigo != null && uo.error == null){
						lstUOSlave = genericosService.obtenerUnidadesOrganicas(uo.codigo, usuarioHiperReg, passwordHiperReg);
						
						if (lstUOSlave != null)
							lstUOB.addAll(lstUOSlave);
					}
					
					//Se introduce un retardo para no sobrecargar el servicio
					Thread.sleep(1000);
					
				}
				
				lstUO.addAll(lstUOB);
				ServiciosGenericosUtils.cargarUnidadesOrganicas(lstUO);
			}catch(Exception e){
				play.Logger.error("Ha ocurrido un error en la carga de Unidades Orgánicas"+e);
				Messages.error("Ha ocurrido un error en la carga de Unidades Orgánicas");
			}
		}
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmCargarUOFichero(String btnCargarUOFichero) {
		checkAuthenticity();
		if (!permisoFrmCargarUOFichero("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			btnCargarUOFicheroFrmCargarUO();
		}

		if (!Messages.hasErrors()) {
			UnidadesOrganicasController.frmCargarUOFicheroValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/UnidadesOrganicas/UnidadesOrganicas.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		UnidadesOrganicasController.frmCargarUOFicheroRender();
	}
	
	@Util
	public static void btnCargarUOFicheroFrmCargarUO() {
		Type type;
		if (new File(Play.applicationPath+"/conf/initial-data/unidadesOrganicas.json").exists()) {
			try {
				type = new TypeToken<ArrayList<ReturnUnidadOrganicaFap>>(){}.getType();
				List<ReturnUnidadOrganicaFap> lstUO = JsonUtils.loadObjectFromJsonFile("conf/initial-data/unidadesOrganicas.json", type);
				ServiciosGenericosUtils.cargarUnidadesOrganicas(lstUO);
			} catch (Exception e) {
				play.Logger.error("No se puede leer el fichero que contiene los parámetros de las Unidades Orgánicas (/conf/initial-data/unidadesOrganicas.json)"+e);
				Messages.error("No se puede leer el fichero que contiene los parámetros de las Unidades Orgánicas (/conf/initial-data/unidadesOrganicas.json)");
			}
		} else {
			play.Logger.info("No se puede leer el fichero que contiene los parámetros de las Unidades Orgánicas (/conf/initial-data/unidadesOrganicas.json)");
			Messages.error("No se encuentra o no existe el fichero que contiene los parámetros de las Unidades Orgánicas (/conf/initial-data/unidadesOrganicas.json)");
		}
	}

}
