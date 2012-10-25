package controllers;

import org.joda.time.DateTime;

import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import messages.Messages;
import models.Aplicacion;
import models.ListaResultadosPeticion;
import models.Peticion;
import models.ServiciosWeb;
import controllers.gen.GraficasWSCMControllerGen;

public class GraficasWSCMController extends GraficasWSCMControllerGen {
	
	public static void index(String accion, Long idAplicacion, Long idServiciosWeb) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("manual/GraficasWS.html");
		}

		Aplicacion aplicacion = GraficasWSCMController.getAplicacion(idAplicacion);

		ServiciosWeb serviciosWeb = null;
		if ("crear".equals(accion)) {
			serviciosWeb = GraficasWSCMController.getServiciosWeb();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				serviciosWeb.save();
				idServiciosWeb = serviciosWeb.id;
				aplicacion.serviciosWeb.add(serviciosWeb);
				aplicacion.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			serviciosWeb = GraficasWSCMController.getServiciosWeb(idAplicacion, idServiciosWeb);
		
		if ((serviciosWeb.peticion.size() == 0) && (serviciosWeb.servicioWebInfo.activo))
			getDatosFromWS(idAplicacion, idServiciosWeb);
		
		log.info("Visitando página: " + "manual/GraficasWS.html");
		renderTemplate("manual/GraficasWS.html", accion, idAplicacion, idServiciosWeb, aplicacion, serviciosWeb);

	}
	
	@Util
	public static void getDatosFromWS(Long idAplicacion, Long idServiciosWeb) {
		Aplicacion aplicacion = GraficasWSCMController.getAplicacion(idAplicacion);
		ServiciosWeb servicioWeb = getServiciosWeb(idAplicacion, idServiciosWeb);
		String urlWS = servicioWeb.servicioWebInfo.urlWS;
		WSRequest request = null;
		JsonElement json = null;
		
		try {
			String url = aplicacion.urlApp + urlWS;
			request = WS.url(url);
			json = request.get().getJson();
		} catch (RuntimeException ce) {
			Messages.warning("El servicio web no existe o no está disponible en estos momentos y no existe historial del mismo");
			play.Logger.error("El servicio web no existe o no está disponible en estos momentos  y no existe historial del mismo");
			Messages.keep();
			redirect("ServiciosWebAppCMController.index", "editar", idAplicacion);
		}
		
		if (json != null) {
			Peticion peticion = new Peticion();
			DateTime hoy = new DateTime();
			peticion.fechaPeticion = hoy.toString();
			peticion.stringJson = json.toString();
			peticion.save();
			servicioWeb.peticion.add(peticion);
			servicioWeb.save();
		}
	}
	
	@Util
	public static void formBtnRecargaDatos(Long idAplicacion, Long idServiciosWeb, String recargasDatos) {
		checkAuthenticity();
		if (!permisoFormBtnRecargaDatos("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			Aplicacion aplicacion = GraficasWSCMController.getAplicacion(idAplicacion);
			ServiciosWeb servicioWeb = getServiciosWeb(idAplicacion, idServiciosWeb);
			
			if (servicioWeb.servicioWebInfo.activo) {
				String urlWS = servicioWeb.servicioWebInfo.urlWS;
				WSRequest request = null;
				JsonElement json = null;
				
				try {
					String url = aplicacion.urlApp + urlWS;
					request = WS.url(url);
					json = request.get().getJson();
				} catch (RuntimeException ce) {
					Messages.warning("El servicio web no existe o no está disponible en estos momentos y no existe historial del mismo");
					play.Logger.error("El servicio web no existe o no está disponible en estos momentos  y no existe historial del mismo");
					Messages.keep();
					redirect("ServiciosWebAppCMController.index", "editar", idAplicacion);
				}
				
				if (json != null) {
					Peticion peticion = new Peticion();
					DateTime hoy = new DateTime();
					peticion.fechaPeticion = hoy.toString();
					peticion.stringJson = json.toString();
					peticion.save();
					servicioWeb.peticion.add(peticion);
					servicioWeb.save();
				}
			}
			else {
				Messages.warning("Está consultando un servicio web del historial, no se puede actualizar");
				play.Logger.error("Está consultando un servicio web del historial, no se puede actualizar");
			}
		}

		if (!Messages.hasErrors()) {
			GraficasWSCMController.formBtnRecargaDatosValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/GraficasWSCM/GraficasWSCM.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/GraficasWSCM/GraficasWSCM.html" + " , intentada sin éxito (Problemas de Validación)");
		GraficasWSCMController.formBtnRecargaDatosRender(idAplicacion, idServiciosWeb);
	}
	
	@Util
	public static void formBtnRecargaDatosRender(Long idAplicacion, Long idServiciosWeb) {
		if (!Messages.hasMessages()) {
			Messages.warning("El servicio web puede haber cambiado, actualice el Servicio Web");
			play.Logger.error("El servicio web puede haber cambiado, actualice el Servicio Web");
			Messages.keep();
			redirect("ServiciosWebAppCMController.index", "editar", idAplicacion);
		}
		Messages.keep();
		redirect("ServiciosWebAppCMController.index", "editar", idAplicacion);
	}
	
}
