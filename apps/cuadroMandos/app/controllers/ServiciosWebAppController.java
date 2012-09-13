package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.sql.Delete;
import org.json.JSONObject;
import org.postgresql.core.Parser;

import messages.Messages;
import models.Aplicacion;
import models.Consulta;
import models.ConsultasWS;
import models.DatoGrafica;
import models.DatosGrafica;
import models.InfoWS;
import models.RelacionWSConsultas;
import models.ServiciosWebAplicacion;
import models.wsTest;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Http.Request;
import play.mvc.Util;
import play.mvc.results.RenderJson;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import controllers.gen.ServiciosWebAppControllerGen;

public class ServiciosWebAppController extends ServiciosWebAppControllerGen {
	
	public static void index(String accion, Long idAplicacion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/ServiciosWebApp/ServiciosWebApp.html");
		}

		Aplicacion aplicacion = null;
		if ("crear".equals(accion)) {
			aplicacion = ServiciosWebAppController.getAplicacion();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				aplicacion.save();
				idAplicacion = aplicacion.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			aplicacion = ServiciosWebAppController.getAplicacion(idAplicacion);

		if (aplicacion.relacionWSConsultas.size() == 0) {
			getWSFromApps(accion, idAplicacion, aplicacion);
		}
		else {
			log.info("Visitando página: " + "gen/ServiciosWebApp/ServiciosWebApp.html");
			renderTemplate("gen/ServiciosWebApp/ServiciosWebApp.html", accion, idAplicacion, aplicacion);
		}
	}
	
	@Util
	public static void getWSFromApps(String accion, Long idAplicacion, Aplicacion aplicacion) {
		
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
				relacion.serviciosWeb = gson.fromJson(array.get(i), ServiciosWebAplicacion.class);
				relacion.save();
				app.relacionWSConsultas.add(relacion);
				app.save();
				i++;
			}
		}
		renderTemplate("gen/ServiciosWebApp/ServiciosWebApp.html", accion, idAplicacion, aplicacion);
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
						relacion.serviciosWeb = gson.fromJson(array.get(j), ServiciosWebAplicacion.class);
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
		ServiciosWebAplicacion anteriorServicioWeb = relacion.get(i).serviciosWeb;
		ServiciosWebAplicacion actualServicioWeb = gson.fromJson(array.get(i), ServiciosWebAplicacion.class);

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
			int infoAnterior = anteriorServicioWeb.servicioWebInfo.size();
			int infoActual = actualServicioWeb.servicioWebInfo.size();
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
						if ((!anteriorServicioWeb.servicioWebInfo.get(j).nombreParam.equals(actualServicioWeb.servicioWebInfo.get(j).nombreParam))
							|| (!anteriorServicioWeb.servicioWebInfo.get(j).tipo.equals(actualServicioWeb.servicioWebInfo.get(j).tipo))) {
							
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
		
		List<RelacionWSConsultas> anterioresRelaciones = RelacionWSConsultas.find("select relacionWSConsultas from Aplicacion aplicacion join aplicacion.relacionWSConsultas relacionWSConsultas where aplicacion.id=?  and relacionWSConsultas.serviciosWeb.activo=true", idAplicacion).fetch();
		int i = 0;
		int anteriorNumRelaciones = anterioresRelaciones.size();		
		
		while (i < anteriorNumRelaciones) {
			String urlWS = anterioresRelaciones.get(i).serviciosWeb.urlWS;
			WSRequest request = null;
			JsonElement json = null;
			
			try {
				request = WS.url(urlWS);
				json = request.get().getJson();
			} catch (RuntimeException ce) {
				Messages.warning("El servicio web no está disponible en estos momentos");
				play.Logger.error("El servicio web no está disponible en estos momentos");
			}
			
			if (json != null) {
				List<ConsultasWS> anterioresConsultas = anterioresRelaciones.get(i).consulta;
				JsonArray array = json.getAsJsonArray();		
				Gson gson = new Gson();
				int j = 0;
				
				while (j < array.size()) {
					ConsultasWS nuevaConsulta = gson.fromJson(array.get(j), ConsultasWS.class);
					if ((anterioresConsultas.size() < array.size()) || (anterioresConsultas.size() > array.size())) {
						// Si hay más o menos consultas se actualiza.
						recargasWSFormBtnRecargaWS(idAplicacion);
					}
					else {
						List<DatosGrafica> nuevosDatosGrafica = nuevaConsulta.datosGrafica;
						List<DatosGrafica> anteriorDatosGrafica = anterioresConsultas.get(j).datosGrafica;
						if (nuevosDatosGrafica.size() == anteriorDatosGrafica.size()) {
							// Si tienen el mismo número de datos que antes, se comprueba
							// que todos sus atributos concuerden, si no se actualiza.
							for (int k = 0; k < nuevosDatosGrafica.size(); k++) {
								List<DatoGrafica> anteriorDato = anteriorDatosGrafica.get(k).datoGrafica;
								List<DatoGrafica> nuevoDato = nuevosDatosGrafica.get(k).datoGrafica;
								for (int l = 0; l < nuevoDato.size(); l++) {
									if ((!anteriorDato.get(l).tituloDato.equals(nuevoDato.get(l).tituloDato))
											|| (anteriorDato.get(l).valorBoolean != nuevoDato.get(l).valorBoolean)
											|| (anteriorDato.get(l).valorDouble != nuevoDato.get(l).valorDouble)
											|| (anteriorDato.get(l).valorFecha != nuevoDato.get(l).valorFecha)
											|| (anteriorDato.get(l).valorLong != nuevoDato.get(l).valorLong)
											|| (!anteriorDato.get(l).valorString.equals(nuevoDato.get(l).valorString))) {
										recargasWSFormBtnRecargaWS(idAplicacion);
									}
								}
							}
						}
						else {
							// Hay más o menos información que antes, por lo que se actualiza.
							recargasWSFormBtnRecargaWS(idAplicacion);
						}
					}
					j++;
				}
			}
			i++;
		}
	}
	
	/**
	 * Tabla en la que se muestran los servicios web activos.
	 * @param idAplicacion
	 */
	public static void tablaserviciosWeb(Long idAplicacion) {
		java.util.List<RelacionWSConsultas> rows = RelacionWSConsultas.find("select relacionWSConsultas from Aplicacion aplicacion join aplicacion.relacionWSConsultas relacionWSConsultas where aplicacion.id=?", idAplicacion).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<RelacionWSConsultas> rowsFiltered = new ArrayList<RelacionWSConsultas>();

		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).serviciosWeb.activo)
				rowsFiltered.add(rows.get(i));
		}
		
		tables.TableRenderResponse<RelacionWSConsultas> response = new tables.TableRenderResponse<RelacionWSConsultas>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("serviciosWeb.nombre", "serviciosWeb.urlWS", "serviciosWeb.activo", "id"));
	}
	
	/**
	 * Tabla en la que solo se muestra un historial de servicios web que ya no están activos.
	 * @param idAplicacion
	 */	
	public static void tablahistorialServiciosWeb(Long idAplicacion) {

		java.util.List<RelacionWSConsultas> rows = RelacionWSConsultas.find("select relacionWSConsultas from Aplicacion aplicacion join aplicacion.relacionWSConsultas relacionWSConsultas where aplicacion.id=?", idAplicacion).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<RelacionWSConsultas> rowsFiltered = new ArrayList<RelacionWSConsultas>();

		for (int i = 0; i < rows.size(); i++) {
			if (!rows.get(i).serviciosWeb.activo)
				rowsFiltered.add(rows.get(i));
		}

		tables.TableRenderResponse<RelacionWSConsultas> response = new tables.TableRenderResponse<RelacionWSConsultas>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("serviciosWeb.nombre", "serviciosWeb.urlWS", "serviciosWeb.activo", "id"));
	}
	
}
