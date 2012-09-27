package controllers;

import messages.Messages;
import models.Aplicacion;
import models.ConsultasWS;
import models.DatosGraficas;
import models.RelacionWSConsultas;
import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import controllers.gen.GraficasWSControllerGen;

public class GraficasWSController extends GraficasWSControllerGen {
	
	public static void index(String accion, Long idAplicacion, Long idRelacionWSConsultas) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/GraficasWS/GraficasWS.html");
		}

		Aplicacion aplicacion = GraficasWSController.getAplicacion(idAplicacion);

		RelacionWSConsultas relacionWSConsultas = null;
		if ("crear".equals(accion)) {
			relacionWSConsultas = GraficasWSController.getRelacionWSConsultas();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				relacionWSConsultas.save();
				idRelacionWSConsultas = relacionWSConsultas.id;
				aplicacion.relacionWSConsultas.add(relacionWSConsultas);
				aplicacion.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			relacionWSConsultas = GraficasWSController.getRelacionWSConsultas(idAplicacion, idRelacionWSConsultas);

		if ((relacionWSConsultas.datos.size() == 0) && (relacionWSConsultas.serviciosWeb.activo))
			getDatosFromWS(idAplicacion, idRelacionWSConsultas);
		
		log.info("Visitando página: " + "app/views/fap/Graficas/GraficasWS.html");
		renderTemplate("app/views/fap/Graficas/GraficasWS.html", accion, idAplicacion, idRelacionWSConsultas, aplicacion, relacionWSConsultas);
	}
	
	@Util
	public static void getDatosFromWS(Long idAplicacion, Long idRelacionWSConsulta) {
		Aplicacion aplicacion = GraficasWSController.getAplicacion(idAplicacion);
		RelacionWSConsultas relacion = getRelacionWSConsultas(idAplicacion, idRelacionWSConsulta);
		String urlWS = relacion.serviciosWeb.urlWS;
		WSRequest request = null;
		JsonElement json = null;
		
		try {
			String url = aplicacion.urlApp + urlWS;
			request = WS.url(url);
//			request = WS.url("http://localhost:9009"+urlWS);
			json = request.get().getJson();
		} catch (RuntimeException ce) {
			Messages.warning("El servicio web no está disponible en estos momentos y no existe historial del mismo");
			play.Logger.error("El servicio web no está disponible en estos momentos  y no existe historial del mismo");
			Messages.keep();
			redirect("ServiciosWebAppController.index", "editar", idAplicacion);
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
