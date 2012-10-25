package controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import oauth.signpost.http.HttpRequest;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.ws.security.handler.RequestData;

import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Util;
import play.utils.HTML;
import properties.FapProperties;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ning.http.client.RequestBuilder;
import com.sun.net.httpserver.HttpContext;

import messages.Messages;
import models.Aplicacion;
import models.ServicioWebInfo;
import models.ServiciosWeb;
import controllers.gen.ServiciosWebAppControllerGen;

public class ServiciosWebAppController extends ServiciosWebAppControllerGen {
	
	public static void index(String accion, Long idAplicacion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/ServiciosWebApp/ServiciosWebApp.html");
		}

		// Obtenemos el nombre de la aplicación y y la url de la misma.
		String appName = FapProperties.get("application.name");
		String path = FapProperties.get("http.path");
		Aplicacion aplicacion = Aplicacion.find("select aplicacion from Aplicacion aplicacion where aplicacion.nombreApp=?", appName).first();
		if (aplicacion == null) {
			aplicacion = ServiciosWebAppController.getAplicacion();
			aplicacion.nombreApp = appName;
			String host = Http.Request.current.get().host;
			if (path != null)
				aplicacion.urlApp = "http://" + host + path + "/WSInfo";
			else
				aplicacion.urlApp = "http://" + host + "/WSInfo";
			aplicacion.save();
			
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {
				aplicacion.save();
				idAplicacion = aplicacion.id;
				accion = "editar";
			}
		}
		
		idAplicacion = aplicacion.id;
		// En el caso que sea la primera vez que se entra a ver los WS
		// se cargan por primera vez, si no es la primera vez se muestran
		// los que estén en BBDD.
		if (aplicacion.serviciosWeb.size() == 0)
			getWS(accion, idAplicacion, aplicacion);
		else {
			log.info("Visitando página: " + "gen/ServiciosWebApp/ServiciosWebApp.html");
			renderTemplate("gen/ServiciosWebApp/ServiciosWebApp.html", accion, idAplicacion, aplicacion);
		}
			
	}
	
	/**
	 * Función que obtiene los servicios de web de la aplicación la primera vez.
	 * @param accion
	 * @param idAplicacion
	 * @param aplicacion
	 */
	@Util
	public static void getWS(String accion, Long idAplicacion, Aplicacion aplicacion) {
		
		String urlApp = aplicacion.urlApp;
		WSRequest request = null;
		JsonElement json = null;
		
		try {
			request = WS.url(urlApp);
			json = request.get().getJson();
		} catch (RuntimeException ce) {
			Messages.warning("El servicio web no existe o no está disponible en estos momentos");
			play.Logger.error("El servicio web no existe o no está disponible en estos momentos");
		} 
		
		if (json != null) {
			JsonArray array = json.getAsJsonArray();		
			Gson gson = new Gson();
			int i = 0;

			// Comprobamos primero que el JSON contiene algo
			if (!array.get(i).toString().equals("null")) {
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
		}
		log.info("Visitando página: " + "gen/ServiciosWebApp/ServiciosWebApp.html");
		renderTemplate("gen/ServiciosWebApp/ServiciosWebApp.html", accion, idAplicacion, aplicacion);
	
	}
	
	@Util
	public static void formBtnRecargaWS(Long idAplicacion, String recargasWS) {
		checkAuthenticity();
		if (!permisoFormBtnRecargaWS("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			recargaWS(idAplicacion);
		}

		if (!Messages.hasErrors()) {
			ServiciosWebAppController.formBtnRecargaWSValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/ServiciosWebApp/ServiciosWebApp.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/ServiciosWebApp/ServiciosWebApp.html" + " , intentada sin éxito (Problemas de Validación)");
		ServiciosWebAppController.formBtnRecargaWSRender(idAplicacion);
	}
	
	
	/**
	 * Función que se ejecuta cuando se pulse el botón de "Recargar Servicios Web".
	 * Comprueba cada WS. Si hay alguna diferencia en la información del mismo, el
	 * primero pasa al historial y se crea un nuevo WS con la información nueva.
	 * @param idAplicacion
	 */
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
			Messages.warning("El servicio web no existe o no está disponible en estos momentos");
			play.Logger.error("El servicio web no existe o no está disponible en estos momentos");
		}
		
		if (json != null) {
			JsonArray array = json.getAsJsonArray();		
			List<ServiciosWeb> anteriorServicioWeb = ServiciosWeb.find("select serviciosWeb from Aplicacion aplicacion join aplicacion.serviciosWeb serviciosWeb where aplicacion.id=? and serviciosWeb.servicioWebInfo.activo=true", idAplicacion).fetch();
			int numWSNuevos = array.size();
			int anteriorNumWS = anteriorServicioWeb.size();
			List<ServiciosWeb> listaActivos = new ArrayList<ServiciosWeb>();
			Gson gson = new Gson();
		
			if (numWSNuevos <= anteriorNumWS) {
				ServicioWebInfo swiActual = null;
				// Se comprueban los WS que siguen iguales.
				for (int i = 0; i < anteriorNumWS; i++) {
					ServicioWebInfo swi = anteriorServicioWeb.get(i).servicioWebInfo;
					boolean iguales = false;
					for (int j = 0; j < numWSNuevos; j++) {
						swiActual = gson.fromJson(array.get(j), ServicioWebInfo.class);
						
						if (((swi.nombre.equals(swiActual.nombre)) && (swi.urlWS.equals(swiActual.urlWS)))) {
							int infoAnterior = swi.infoParams.size();
							int infoActual = swiActual.infoParams.size();
							
							if (infoAnterior == infoActual) {
								for (int k = 0; k < infoActual; k++) {
									if ((swi.infoParams.get(k).nombreParam.equals(swiActual.infoParams.get(k).nombreParam))
										&& (swi.infoParams.get(k).tipo.equals(swiActual.infoParams.get(k).tipo))) {
											iguales = true;
											
									}
								}
								if (iguales)
									listaActivos.add(anteriorServicioWeb.get(i));
							}
						}	
					}
				}
				
				// Los WS que no están en la lista de activos y estaban
				// antes se pasan al historial.
				for (int k = 0; k < anteriorNumWS; k++) {
					if (!listaActivos.contains(anteriorServicioWeb.get(k))) {
						anteriorServicioWeb.get(k).servicioWebInfo.activo = false;
						anteriorServicioWeb.get(k).save();
					}
				}

				// Se añaden los WS nuevos (que no están en BBDD).
				for (int k = 0; k < numWSNuevos; k++) {
					swiActual = gson.fromJson(array.get(k), ServicioWebInfo.class);
					ServiciosWeb swNuevo = ServiciosWeb.find("select serviciosWeb from Aplicacion aplicacion join aplicacion.serviciosWeb serviciosWeb where aplicacion.id=? and serviciosWeb.servicioWebInfo.activo=true and serviciosWeb.servicioWebInfo.nombre=?", idAplicacion, swiActual.nombre).first();
					if (swNuevo == null) {
						swiActual.activo = true;
						swiActual.save();
						ServiciosWeb ws = new ServiciosWeb();
						ws.servicioWebInfo = swiActual;
						ws.save();
						app.serviciosWeb.add(ws);
						app.save();
					}
				}
			} else {
				if (numWSNuevos > anteriorNumWS) {
					ServicioWebInfo swiActual = null;
					// Se comprueban los WS que siguen iguales.
					for (int i = 0; i < numWSNuevos; i++) {
						swiActual = gson.fromJson(array.get(i), ServicioWebInfo.class);
						boolean iguales = false;
						for (int j = 0; j < anteriorNumWS; j++) {
							ServicioWebInfo swi = anteriorServicioWeb.get(j).servicioWebInfo;
							
							if (((swi.nombre.equals(swiActual.nombre)) && (swi.urlWS.equals(swiActual.urlWS)))) {
								int infoAnterior = swi.infoParams.size();
								int infoActual = swiActual.infoParams.size();
								
								if (infoAnterior == infoActual) {
									for (int k = 0; k < infoActual; k++) {
										if ((swi.infoParams.get(k).nombreParam.equals(swiActual.infoParams.get(k).nombreParam))
											&& (swi.infoParams.get(k).tipo.equals(swiActual.infoParams.get(k).tipo))) {
												iguales = true;
										}
									}
									if (iguales)
										listaActivos.add(anteriorServicioWeb.get(j));
								}
							}
						}
					}

					// Los WS que no están en la lista de activos y estaban
					// antes se pasan al historial.
					for (int k = 0; k < anteriorNumWS; k++) {
						if (!listaActivos.contains(anteriorServicioWeb.get(k))) {
							anteriorServicioWeb.get(k).servicioWebInfo.activo = false;
							anteriorServicioWeb.get(k).save();
						}
					}

					// Se añaden los WS nuevos (que no están en BBDD).
					for (int k = 0; k < numWSNuevos; k++) {
						swiActual = gson.fromJson(array.get(k), ServicioWebInfo.class);
						ServiciosWeb swNuevo = ServiciosWeb.find("select serviciosWeb from Aplicacion aplicacion join aplicacion.serviciosWeb serviciosWeb where aplicacion.id=? and serviciosWeb.servicioWebInfo.activo=true and serviciosWeb.servicioWebInfo.nombre=?", idAplicacion, swiActual.nombre).first();
						if (swNuevo == null) {
							swiActual.activo= true;
							swiActual.save();
							ServiciosWeb ws = new ServiciosWeb();
							ws.servicioWebInfo = swiActual;
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
