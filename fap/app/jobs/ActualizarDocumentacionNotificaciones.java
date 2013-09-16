package jobs;

import models.AdministracionFapJobs;
import play.db.jpa.Transactional;
import play.jobs.Job;
import play.jobs.On;
import properties.FapProperties;
import utils.NotificacionUtils;

@On("0 0 1 * * ?")
public class ActualizarDocumentacionNotificaciones extends Job {
		
	static Integer tiempoRefresco = 1;
		
	@Transactional
    public void doJob() {
		if (AdministracionFapJobs.all() != null) {
			AdministracionFapJobs job = AdministracionFapJobs.all().first();
			if (job.actualizarNotificaciones) {
		    	if ((job.valorActualizarNotificaciones != null) && (tiempoRefresco == job.valorActualizarNotificaciones)){
		    		tiempoRefresco=1;
			    	if ((FapProperties.get("fap.notificacion.activa") != null) && (FapProperties.getBoolean("fap.notificacion.activa")) && (FapProperties.get("fap.notificacion.procedimiento") != null) && (!(FapProperties.get("fap.notificacion.procedimiento").trim().isEmpty()))){
			    		NotificacionUtils.recargarDocumentosNotificacionesFromWS(FapProperties.get("fap.notificacion.procedimiento"));
			    	}
		    	}
			}
		}
	}
}
