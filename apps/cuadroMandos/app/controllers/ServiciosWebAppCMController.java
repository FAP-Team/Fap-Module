package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import messages.Messages;
import models.Aplicacion;
import models.ServicioWebInfo;
import models.ServiciosWeb;
import controllers.gen.ServiciosWebAppCMControllerGen;

public class ServiciosWebAppCMController extends ServiciosWebAppCMControllerGen {
	public static void index(String accion, Long idAplicacion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/ServiciosWebAppCM/ServiciosWebAppCM.html");
		}

		Aplicacion aplicacion = null;
		if ("crear".equals(accion)) {
			aplicacion = ServiciosWebAppCMController.getAplicacion();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				aplicacion.save();
				idAplicacion = aplicacion.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			aplicacion = ServiciosWebAppCMController.getAplicacion(idAplicacion);
		if (aplicacion.serviciosWeb.size() == 0) {
			getWS(accion, idAplicacion, aplicacion);
		}
		else {
			log.info("Visitando página: " + "gen/ServiciosWebAppCM/ServiciosWebAppCM.html");
			renderTemplate("gen/ServiciosWebAppCM/ServiciosWebAppCM.html", accion, idAplicacion, aplicacion);
	
		}
	}
	
	@Util
	public static void getWS(String accion, Long idAplicacion, Aplicacion aplicacion) {
		
		String urlApp = aplicacion.urlApp;
		WSRequest request = null;
		JsonElement json = null;
		
		try {
			request = WS.url(urlApp);
			json = request.get().getJson();
		} catch (RuntimeException ce) {
			Messages.warning("El servicio web no está disponible en estos momentos");
			play.Logger.error("El servicio web no está disponible en estos momentos");
		} 
		
		if (json != null) {
			JsonArray array = json.getAsJsonArray();		
			Gson gson = new Gson();
			int i = 0;
			
			while (i < array.size()) {
				ServiciosWeb servicioWeb = new ServiciosWeb();
				servicioWeb.servicioWebInfo = gson.fromJson(array.get(i), ServicioWebInfo.class);
				servicioWeb.servicioWebInfo.activo = true;
				servicioWeb.save();
				aplicacion.serviciosWeb.add(servicioWeb);
				aplicacion.save();
				i++;
			}
		}
		log.info("Visitando página: " + "gen/ServiciosWebAppCM/ServiciosWebAppCM.html");
		renderTemplate("gen/ServiciosWebAppCM/ServiciosWebAppCM.html", accion, idAplicacion, aplicacion);

	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formBtnRecargaWS(Long idAplicacion, String recargasWS) {
		checkAuthenticity();
		if (!permisoFormBtnRecargaWS("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			recargaWS(idAplicacion);
		}

		if (!Messages.hasErrors()) {
			ServiciosWebAppCMController.formBtnRecargaWSValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/ServiciosWebAppCM/ServiciosWebAppCM.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/ServiciosWebAppCM/ServiciosWebAppCM.html" + " , intentada sin éxito (Problemas de Validación)");
		ServiciosWebAppCMController.formBtnRecargaWSRender(idAplicacion);
	}
	
	@Util
	public static void recargaWS(Long idAplicacion) {
		
		Aplicacion app = getAplicacion(idAplicacion);
		String urlApp = app.urlApp;
		WSRequest request = null;
		JsonElement json = null;
		try {
			request = WS.url(urlApp);
			json = request.get().getJson();
		} catch (RuntimeException ce) {
			Messages.warning("El servicio web no está disponible en estos momentos");
			play.Logger.error("El servicio web no está disponible en estos momentos");
		}
		
		if (json != null) {
			JsonArray array = json.getAsJsonArray();		
			int i = 0;
			List<ServiciosWeb> anteriorServicioWeb = ServiciosWeb.find("select serviciosWeb from Aplicacion aplicacion join aplicacion.serviciosWeb serviciosWeb where aplicacion.id=? and serviciosWeb.servicioWebInfo.activo=true", idAplicacion).fetch();
			int numWS = array.size();
			int anteriorNumWS = anteriorServicioWeb.size();
			if (numWS < anteriorNumWS) {
				while (i < numWS) {
					diferentesWS(array, i, anteriorServicioWeb, app);
					i++;
				}
				for (int j = i; j < anteriorNumWS; j++) {
					anteriorServicioWeb.get(j).servicioWebInfo.activo = false;
					anteriorServicioWeb.get(j).servicioWebInfo.save();
				}
			} else {
				i = 0;
				if (numWS > anteriorNumWS) {
					while (i < anteriorNumWS) {
						diferentesWS(array, i, anteriorServicioWeb, app);
						i++;
					}
					for (int j = i; j < numWS; j++) {
						Gson gson = new Gson();
						ServiciosWeb servicioWeb = new ServiciosWeb();
						servicioWeb.servicioWebInfo = gson.fromJson(array.get(j), ServicioWebInfo.class);
						servicioWeb.save();
						servicioWeb.servicioWebInfo.activo = true;
						servicioWeb.servicioWebInfo.save();
						app.serviciosWeb.add(servicioWeb);
						app.save();
					}
				}
				else {
					while (i < anteriorNumWS) {
						diferentesWS(array, i, anteriorServicioWeb, app);
						i++;
					}
				}
			}
		}
	}
	
	@Util
	public static void diferentesWS(JsonArray array, int i, List<ServiciosWeb> serviciosWeb, Aplicacion app) {
	
		Gson gson = new Gson();
		ServicioWebInfo anteriorServicioWebInfo = serviciosWeb.get(i).servicioWebInfo;
		ServicioWebInfo actualServicioWebInfo = gson.fromJson(array.get(i), ServicioWebInfo.class);
	
		if (((!anteriorServicioWebInfo.nombre.equals(actualServicioWebInfo.nombre)) ||
			(!anteriorServicioWebInfo.urlWS.equals(actualServicioWebInfo.urlWS)))) {
	
			anteriorServicioWebInfo.activo = false;
			anteriorServicioWebInfo.save();
			actualServicioWebInfo.activo= true;
			actualServicioWebInfo.save();
			ServiciosWeb ws = new ServiciosWeb();
			ws.servicioWebInfo = actualServicioWebInfo;
			ws.save();
			app.serviciosWeb.add(ws);
			app.save();
		}
		else {
			int infoAnterior = anteriorServicioWebInfo.infoParams.size();
			int infoActual = actualServicioWebInfo.infoParams.size();
			if (infoAnterior < infoActual) {
				anteriorServicioWebInfo.activo = false;
				anteriorServicioWebInfo.save();
				actualServicioWebInfo.activo= true;
				actualServicioWebInfo.save();
				ServiciosWeb ws = new ServiciosWeb();
				ws.servicioWebInfo = actualServicioWebInfo;
				ws.save();
				app.serviciosWeb.add(ws);
				app.save();
			}
			else {
				if (infoAnterior > infoActual) {
					anteriorServicioWebInfo.activo = false;
					anteriorServicioWebInfo.save();
					actualServicioWebInfo.activo= true;
					actualServicioWebInfo.save();
					ServiciosWeb ws = new ServiciosWeb();
					ws.servicioWebInfo = actualServicioWebInfo;
					ws.save();
					app.serviciosWeb.add(ws);
					app.save();
				}
				else {
					for (int j = 0; j < infoActual; j++) {
						if ((!anteriorServicioWebInfo.infoParams.get(j).nombreParam.equals(actualServicioWebInfo.infoParams.get(j).nombreParam))
							|| (!anteriorServicioWebInfo.infoParams.get(j).tipo.equals(actualServicioWebInfo.infoParams.get(j).tipo))) {
							
							anteriorServicioWebInfo.activo = false;
							anteriorServicioWebInfo.save();
							actualServicioWebInfo.activo= true;
							actualServicioWebInfo.save();
							ServiciosWeb ws = new ServiciosWeb();
							ws.servicioWebInfo = actualServicioWebInfo;
							ws.save();
							app.serviciosWeb.add(ws);
							app.save();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Tabla en la que se muestran los servicios web activos.
	 * @param idAplicacion
	 */
	public static void tablaserviciosWeb(Long idAplicacion) {
		java.util.List<ServiciosWeb> rows = ServiciosWeb.find("select serviciosWeb from Aplicacion aplicacion join aplicacion.serviciosWeb serviciosWeb where aplicacion.id=?", idAplicacion).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<ServiciosWeb> rowsFiltered = new ArrayList<ServiciosWeb>();
		
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).servicioWebInfo.activo)
				rowsFiltered.add(rows.get(i));
		}
		
		tables.TableRenderResponse<ServiciosWeb> response = new tables.TableRenderResponse<ServiciosWeb>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("servicioWebInfo.nombre", "servicioWebInfo.urlWS", "id"));
	}
	
	/**
	 * Tabla en la que solo se muestra un historial de servicios web que ya no están activos.
	 * @param idAplicacion
	 */
	public static void tablahistorialServiciosWeb(Long idAplicacion) {
		java.util.List<ServiciosWeb> rows = ServiciosWeb.find("select serviciosWeb from Aplicacion aplicacion join aplicacion.serviciosWeb serviciosWeb where aplicacion.id=?", idAplicacion).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<ServiciosWeb> rowsFiltered = new ArrayList<ServiciosWeb>();
		
		for (int i = 0; i < rows.size(); i++) {
			if (!rows.get(i).servicioWebInfo.activo)
				rowsFiltered.add(rows.get(i));
		}
		
		tables.TableRenderResponse<ServiciosWeb> response = new tables.TableRenderResponse<ServiciosWeb>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("servicioWebInfo.nombre", "servicioWebInfo.urlWS", "id"));
	}
}
