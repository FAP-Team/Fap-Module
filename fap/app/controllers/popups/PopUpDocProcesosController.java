
package controllers.popups;

import javax.persistence.EntityTransaction;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.SolicitudGenerica;
import play.db.jpa.JPA;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import controllers.fap.AgenteController;
import controllers.gen.popups.PopUpDocProcesosControllerGen;

public class PopUpDocProcesosController extends PopUpDocProcesosControllerGen {

	@javax.inject.Inject
	static GestorDocumentalService gestorDocumentalService;

	//Borra el documento de la BD. Se borra también del AED si no está clasificado
	public static void borrar(Long idSolicitud, Long idDocumento) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		Documento dbDocumento = PopUpDocProcesosController.getDocumento(idSolicitud, idDocumento);
		SolicitudGenerica dbSolicitud = PopUpDocProcesosController.getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			PopUpDocProcesosController.borrarValidateRules(dbDocumento);
		}

		if (!Messages.hasErrors()) {

		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			EntityTransaction tx = JPA.em().getTransaction();
			try {
				if (tx.isActive())
					tx.commit();

				tx.begin();

				dbSolicitud.aportaciones.actual.documentos.remove(dbDocumento);
				dbSolicitud.save();

				dbDocumento.delete();

				play.Logger.info("Eliminando documento " + dbDocumento.uri + " de la base de datos");

				try {
					gestorDocumentalService.deleteDocumento(dbDocumento);
					play.Logger.info("Documento eliminado de la base de datos y del AED");
				} catch (GestorDocumentalServiceException e) {
					e.printStackTrace();
					play.Logger.info("Ha ocurrido un error eliminando el documento del AED");
				}

				tx.commit();
			} catch (Exception e) {
				if ((tx != null) && (tx.isActive()))
					tx.rollback();
				play.Logger.info("Se ha producido un error eliminando el documento");
			}

			log.info("Acción Borrar de página: " + "gen/popups/PopUpDocProcesos.html" + " , intentada con éxito" + " Agente: " + logAgente);
		} else {
			log.info("Acción Borrar de página: " + "gen/popups/PopUpDocProcesos.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		}
		PopUpDocProcesosController.borrarRender(idSolicitud, idDocumento);
	}

}
