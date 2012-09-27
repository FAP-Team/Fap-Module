package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Util;

import messages.Messages;
import models.Aplicacion;
import models.ConsultasWS;
import models.DatosGraficas;
import models.ListaConsultas;
import models.RelacionWSConsultas;
import models.ServiciosWebApp;
import controllers.gen.ServiciosWebAppsControllerGen;

public class ServiciosWebAppsController extends ServiciosWebAppsControllerGen {
	public static void index(String accion, Long idAplicacion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/ServiciosWebApps/ServiciosWebApps.html");
		}

		Aplicacion aplicacion = null;
		if ("crear".equals(accion)) {
			aplicacion = ServiciosWebAppsController.getAplicacion();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				aplicacion.save();
				idAplicacion = aplicacion.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			aplicacion = ServiciosWebAppsController.getAplicacion(idAplicacion);

		if (aplicacion.relacionWSConsultas.size() == 0) {
			getWS(accion, idAplicacion, aplicacion);
		}
		else {
			log.info("Visitando página: " + "gen/ServiciosWebApps/ServiciosWebApps.html");
			renderTemplate("gen/ServiciosWebApps/ServiciosWebApps.html", accion, idAplicacion, aplicacion);
		}
	}
	
	@Util
	public static void getWS(String accion, Long idAplicacion, Aplicacion aplicacion) {
		
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
			Gson gson = new Gson();
			int i = 0;
			
			while (i < array.size()) {
				RelacionWSConsultas relacion = new RelacionWSConsultas();
				relacion.serviciosWeb = gson.fromJson(array.get(i), ServiciosWebApp.class);
				relacion.serviciosWeb.activo = true;
				relacion.save();
				app.relacionWSConsultas.add(relacion);
				app.save();
				i++;
			}
		}
		log.info("Visitando página: " + "gen/ServiciosWebApps/ServiciosWebApps.html");
		renderTemplate("gen/ServiciosWebApps/ServiciosWebApps.html", accion, idAplicacion, aplicacion);
	}
	
	@Util
	public static void recargasWSFormBtnRecargaWS(Long idAplicacion) {
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
			List<RelacionWSConsultas> anteriorRelacionesWSConsultas = RelacionWSConsultas.find("select relacionWSConsultas from Aplicacion aplicacion join aplicacion.relacionWSConsultas relacionWSConsultas where aplicacion.id=? and relacionWSConsultas.serviciosWeb.activo=true", idAplicacion).fetch();
			int numWS = array.size();
			int anteriorNumRelaciones = anteriorRelacionesWSConsultas.size();
			if (numWS < anteriorNumRelaciones) {
				while (i < numWS) {
					diferentesWS(array, i, anteriorRelacionesWSConsultas, app);
					i++;
				}
				for (int j = i; j < anteriorNumRelaciones; j++) {
					anteriorRelacionesWSConsultas.get(j).serviciosWeb.activo = false;
					anteriorRelacionesWSConsultas.get(j).serviciosWeb.save();
				}
			} else {
				i = 0;
				if (numWS > anteriorNumRelaciones) {
					while (i < anteriorNumRelaciones) {
						diferentesWS(array, i, anteriorRelacionesWSConsultas, app);
						i++;
					}
					for (int j = i; j < numWS; j++) {
						Gson gson = new Gson();
						RelacionWSConsultas relacion = new RelacionWSConsultas();
						relacion.serviciosWeb = gson.fromJson(array.get(j), ServiciosWebApp.class);
						relacion.save();
						relacion.serviciosWeb.activo = true;
						relacion.serviciosWeb.save();
						app.relacionWSConsultas.add(relacion);
						app.save();
					}
				}
				else {
					while (i < anteriorNumRelaciones) {
						diferentesWS(array, i, anteriorRelacionesWSConsultas, app);
						i++;
					}
				}
			}
		}
	}
	
	@Util
	public static void diferentesWS(JsonArray array, int i, List<RelacionWSConsultas> relacion, Aplicacion app) {
	
		Gson gson = new Gson();
		ServiciosWebApp anteriorServicioWeb = relacion.get(i).serviciosWeb;
		ServiciosWebApp actualServicioWeb = gson.fromJson(array.get(i), ServiciosWebApp.class);
	
		if (((!anteriorServicioWeb.nombre.equals(actualServicioWeb.nombre)) ||
			(!anteriorServicioWeb.urlWS.equals(actualServicioWeb.urlWS)))) {
	
			anteriorServicioWeb.activo = false;
			anteriorServicioWeb.save();
			actualServicioWeb.activo= true;
			actualServicioWeb.save();
			RelacionWSConsultas rel = new RelacionWSConsultas();
			rel.serviciosWeb = actualServicioWeb;
			rel.save();
			app.relacionWSConsultas.add(rel);
			app.save();
		}
		else {
			int infoAnterior = anteriorServicioWeb.infoRet.size();
			int infoActual = actualServicioWeb.infoRet.size();
			if (infoAnterior < infoActual) {
				anteriorServicioWeb.activo = false;
				anteriorServicioWeb.save();
				actualServicioWeb.activo= true;
				actualServicioWeb.save();
				RelacionWSConsultas rel = new RelacionWSConsultas();
				rel.serviciosWeb = actualServicioWeb;
				rel.save();
				app.relacionWSConsultas.add(rel);
				app.save();
			}
			else {
				if (infoAnterior > infoActual) {
					anteriorServicioWeb.activo = false;
					anteriorServicioWeb.save();
					actualServicioWeb.activo= true;
					actualServicioWeb.save();
					RelacionWSConsultas rel = new RelacionWSConsultas();
					rel.serviciosWeb = actualServicioWeb;
					rel.save();
					app.relacionWSConsultas.add(rel);
					app.save();
				}
				else {
					for (int j = 0; j < infoActual; j++) {
						if ((!anteriorServicioWeb.infoRet.get(j).nombreParam.equals(actualServicioWeb.infoRet.get(j).nombreParam))
							|| (!anteriorServicioWeb.infoRet.get(j).tipo.equals(actualServicioWeb.infoRet.get(j).tipo))) {
							
							anteriorServicioWeb.activo = false;
							anteriorServicioWeb.save();
							actualServicioWeb.activo= true;
							actualServicioWeb.save();
							RelacionWSConsultas rel = new RelacionWSConsultas();
							rel.serviciosWeb = actualServicioWeb;
							rel.save();
							app.relacionWSConsultas.add(rel);
							app.save();
						}
					}
				}
			}
		}
	}

	@Util
	public static void recargasDatosFormBtnRecargaWS(Long idAplicacion) {
		Aplicacion app = getAplicacion(idAplicacion);
		String urlApp = app.urlApp;
		List<RelacionWSConsultas> anterioresRelaciones = RelacionWSConsultas.find("select relacionWSConsultas from Aplicacion aplicacion join aplicacion.relacionWSConsultas relacionWSConsultas where aplicacion.id=?  and relacionWSConsultas.serviciosWeb.activo=true", idAplicacion).fetch();
		int i = 0;
		int anteriorNumRelaciones = anterioresRelaciones.size();		
		
		while (i < anteriorNumRelaciones) {
			String urlWS = anterioresRelaciones.get(i).serviciosWeb.urlWS;
			WSRequest request = null;
			JsonElement json = null;
			try {
				String url = urlApp + urlWS;
				request = WS.url(url);
				json = request.get().getJson();
			} catch (RuntimeException ce) {
				Messages.warning("El servicio web no está disponible en estos momentos");
				play.Logger.error("El servicio web no está disponible en estos momentos");
			}
			
			if ((json != null) && (anterioresRelaciones.get(i).datos.size() > 0)) {
				ListaConsultas anterioresConsultas = anterioresRelaciones.get(i).datos.get(0).lista.get(0);
				Gson gson = new Gson();
				DatosGraficas nuevaConsulta = gson.fromJson(json, DatosGraficas.class);
				ListaConsultas nuevasConsulta = nuevaConsulta.lista.get(0);
				
				if ((anterioresConsultas.consultasWS.size() < nuevasConsulta.consultasWS.size()) || (anterioresConsultas.consultasWS.size() > nuevasConsulta.consultasWS.size())) {
					recargasWSFormBtnRecargaWS(idAplicacion);
				}
				else {
					for (int j = 0; j < anterioresConsultas.consultasWS.size(); j++) {
						ConsultasWS consultaAnt = anterioresConsultas.consultasWS.get(j);
						ConsultasWS consultaPost = nuevasConsulta.consultasWS.get(j);
						for (int k = 0; k < consultaAnt.consultaWS.size(); k++) {
							if ((!consultaAnt.consultaWS.get(k).nombre.equals(consultaPost.consultaWS.get(k).nombre))
									|| (consultaAnt.consultaWS.get(k).valorBoolean != consultaPost.consultaWS.get(k).valorBoolean)
									|| (consultaAnt.consultaWS.get(k).valorDouble != consultaPost.consultaWS.get(k).valorDouble)
									|| (consultaAnt.consultaWS.get(k).valorDateTime != consultaPost.consultaWS.get(k).valorDateTime)
									|| (consultaAnt.consultaWS.get(k).valorLong != consultaPost.consultaWS.get(k).valorLong)
									|| (consultaAnt.consultaWS.get(k).valorString != consultaPost.consultaWS.get(k).valorString)) {
								recargasWSFormBtnRecargaWS(idAplicacion);
							}
						}
					}
				}
				
			}
			i++;
		}
	}
	
	public static void tablaserviciosWeb(Long idAplicacion) {
		java.util.List<RelacionWSConsultas> rows = RelacionWSConsultas.find("select relacionWSConsultas from Aplicacion aplicacion join aplicacion.relacionWSConsultas relacionWSConsultas where aplicacion.id=?", idAplicacion).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<RelacionWSConsultas> rowsFiltered = new ArrayList<RelacionWSConsultas>();

		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).serviciosWeb.activo)
				rowsFiltered.add(rows.get(i));
		}
		
		tables.TableRenderResponse<RelacionWSConsultas> response = new tables.TableRenderResponse<RelacionWSConsultas>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("serviciosWeb.nombre", "serviciosWeb.urlWS", "id"));
	}
	
	public static void tablahistorialServiciosWeb(Long idAplicacion) {
		java.util.List<RelacionWSConsultas> rows = RelacionWSConsultas.find("select relacionWSConsultas from Aplicacion aplicacion join aplicacion.relacionWSConsultas relacionWSConsultas where aplicacion.id=?", idAplicacion).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<RelacionWSConsultas> rowsFiltered = new ArrayList<RelacionWSConsultas>();

		for (int i = 0; i < rows.size(); i++) {
			if (!rows.get(i).serviciosWeb.activo)
				rowsFiltered.add(rows.get(i));
		}
		
		tables.TableRenderResponse<RelacionWSConsultas> response = new tables.TableRenderResponse<RelacionWSConsultas>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("serviciosWeb.nombre", "serviciosWeb.urlWS", "id"));
	}
	
}
