package jobs;

import java.util.List;

import models.Aplicacion;
import models.RelacionWSConsultas;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import controllers.ServiciosWebAppController;

import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Util;

/**
 * Job que actualiza los servicios web, por si se han añadido nuevo o se han eliminado.
 */
@On("0 0 12 * * ?")
public class ActualizarWS extends Job {
	
	public void doJob() {
		List<Aplicacion> listaApp = Aplicacion.findAll();
		for (int numApp = 0; numApp < listaApp.size(); numApp++) {
			Aplicacion app = listaApp.get(numApp);
			ServiciosWebAppController.recargasWSFormBtnRecargaWS(app.id);
			ServiciosWebAppController.recargasDatosFormBtnRecargaWS(app.id);
		}
	}
}
