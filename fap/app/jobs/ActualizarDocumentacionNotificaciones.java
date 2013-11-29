package jobs;

import models.AdministracionFapJobs;
import play.db.jpa.Transactional;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import properties.FapProperties;
import utils.NotificacionUtils;

@On("0 0 10 * * ?")
public class ActualizarDocumentacionNotificaciones extends Job {
		
	@Transactional
    public void doJob() {
		if (AdministracionFapJobs.all() != null) {
			AdministracionFapJobs job = AdministracionFapJobs.all().first();
			if (job.actualizarNotificaciones){
			    if ((FapProperties.get("fap.notificacion.activa") != null) && (FapProperties.getBoolean("fap.notificacion.activa")) && (FapProperties.get("fap.notificacion.procedimiento") != null) && (!(FapProperties.get("fap.notificacion.procedimiento").trim().isEmpty()))){
			    	play.Logger.info("Iniciando Job de Recarga de Documentos de Notificaciones");
			    	NotificacionUtils.recargarDocumentosNotificacionesFromWS(FapProperties.get("fap.notificacion.procedimiento"));
			    }
			}
		}
	}
}
