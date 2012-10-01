package jobs;

import java.util.List;

import models.AdministracionFapJobs;
import models.Aplicacion;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import controllers.ServiciosWebAppController;

/**
 * Job que actualiza los servicios web, por si se han añadido nuevos o se han eliminado.
 */
@On("0 0 12 * * ?")
public class ActualizarServiciosWeb extends Job {

	public void doJob() {
		if (AdministracionFapJobs.all() != null) {
			AdministracionFapJobs job = AdministracionFapJobs.all().first();
			if (job.actualizarServiciosWeb) {
				List<Aplicacion> listaApp = Aplicacion.findAll();
				for (int numApp = 0; numApp < listaApp.size(); numApp++) {
					Aplicacion app = listaApp.get(numApp);
					play.Logger.info("Actualizando información de servicios web de la aplicación: "+app.nombreApp);
					ServiciosWebAppController.recargaWS(app.id);
				}
			}
		}
	}
}
