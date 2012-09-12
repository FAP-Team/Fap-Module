package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import messages.Messages;
import models.Aplicacion;
import models.ConsultasWS;
import models.RelacionWSConsultas;
import models.ServiciosWebAplicacion;
import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Util;
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

		if ((relacionWSConsultas.consulta.size() == 0) && (relacionWSConsultas.serviciosWeb.activo))
			getDatosFromWS(idAplicacion, idRelacionWSConsultas);

		
		log.info("Visitando página: " + "app/views/manual/GraficasWS.html");
		renderTemplate("app/views/manual/GraficasWS.html", accion, idAplicacion, idRelacionWSConsultas, aplicacion, relacionWSConsultas);
	}
	
	@Util
	public static void getDatosFromWS(Long idAplicacion, Long idRelacionWSConsulta) {
		Aplicacion app = Aplicacion.findById(idAplicacion);
		RelacionWSConsultas relacion = getRelacionWSConsultas(idAplicacion, idRelacionWSConsulta);
		String urlWS = relacion.serviciosWeb.urlWS;
		WSRequest request = WS.url(urlWS);
		
		if (request.body != null) {
			JsonElement json = request.get().getJson();
			JsonArray array = json.getAsJsonArray();
			Gson gson = new Gson();
			int i = 0;
			while (i < array.size()) {
				ConsultasWS consulta = gson.fromJson(array.get(i), ConsultasWS.class);
				consulta.save();
				relacion.consulta.add(consulta);
				relacion.save();
				app.save();
				i++;
			}
		}
	}
}
