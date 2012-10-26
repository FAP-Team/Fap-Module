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
								if (iguales) {
									// Comprobamos si solo la descripción ha cambiado y, en tal caso,
									// actualizamos esa información.
									if (!comprobarDescripcion(swi, swiActual)) {
										anteriorServicioWeb.get(i).servicioWebInfo.descripcion = swiActual.descripcion;
										anteriorServicioWeb.get(i).save();
									}
									listaActivos.add(anteriorServicioWeb.get(i));
								}
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
									if (iguales) {
										// Comprobamos si solo la descripción ha cambiado y, en tal caso,
										// actualizamos esa información.
										if (!comprobarDescripcion(swi, swiActual)) {
											anteriorServicioWeb.get(j).servicioWebInfo.descripcion = swiActual.descripcion;
											anteriorServicioWeb.get(j).save();
										}
										listaActivos.add(anteriorServicioWeb.get(j));
									}
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
	
	private static boolean comprobarDescripcion(ServicioWebInfo swiAnterior, ServicioWebInfo swiNuevo) {
		
		if (swiAnterior.descripcion.equals(swiNuevo.descripcion))
			return true;

		return false;		
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
		renderJSON(response.toJSON("servicioWebInfo.nombre", "servicioWebInfo.urlWS", "id", "servicioWebInfo.descripcion"));
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
		renderJSON(response.toJSON("servicioWebInfo.nombre", "servicioWebInfo.urlWS", "id", "servicioWebInfo.descripcion"));
	}
}
