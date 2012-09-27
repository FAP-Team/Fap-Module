package controllers;

import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import messages.Messages;
import models.Aplicacion;
import models.DatosGraficas;
import models.RelacionWSConsultas;
import controllers.gen.GraficasWSAppControllerGen;

public class GraficasWSAppController extends GraficasWSAppControllerGen {
	public static void index(String accion, Long idAplicacion, Long idRelacionWSConsultas) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/GraficasWSApp/GraficasWSApp.html");
		}

		Aplicacion aplicacion = GraficasWSAppController.getAplicacion(idAplicacion);

		RelacionWSConsultas relacionWSConsultas = null;
		if ("crear".equals(accion)) {
			relacionWSConsultas = GraficasWSAppController.getRelacionWSConsultas();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				relacionWSConsultas.save();
				idRelacionWSConsultas = relacionWSConsultas.id;
				aplicacion.relacionWSConsultas.add(relacionWSConsultas);
				aplicacion.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			relacionWSConsultas = GraficasWSAppController.getRelacionWSConsultas(idAplicacion, idRelacionWSConsultas);
		
		if ((relacionWSConsultas.datos.size() == 0) && (relacionWSConsultas.serviciosWeb.activo))
			getDatosFromWS(idAplicacion, idRelacionWSConsultas);
		// else coger de base datos xq WS caido
		
		log.info("Visitando página: " + "app/views/manual/GraficasWS.html");
		renderTemplate("app/views/manual/GraficasWS.html", accion, idAplicacion, idRelacionWSConsultas, aplicacion, relacionWSConsultas);
	}
	
	@Util
	public static void getDatosFromWS(Long idAplicacion, Long idRelacionWSConsulta) {
		Aplicacion app = GraficasWSAppController.getAplicacion(idAplicacion);
		RelacionWSConsultas relacion = getRelacionWSConsultas(idAplicacion, idRelacionWSConsulta);
		String urlWS = relacion.serviciosWeb.urlWS;
		WSRequest request = null;
		JsonElement json = null;
		
		try {
			String url = app.urlApp + urlWS;
			request = WS.url(url);
			json = request.get().getJson();
		} catch (RuntimeException ce) {
			Messages.warning("El servicio web no está disponible en estos momentos y no existe historial del mismo");
			play.Logger.error("El servicio web no está disponible en estos momentos  y no existe historial del mismo");
			Messages.keep();
			redirect("ServiciosWebAppsController.index", "editar", idAplicacion);
		}
		
		if (json != null) {
			Gson gson = new Gson();
			DatosGraficas datos = gson.fromJson(json, DatosGraficas.class);
			datos.save();
			relacion.datos.add(datos);
			relacion.save();

		}
	}
	
}
