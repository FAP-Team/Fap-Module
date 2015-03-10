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
import controllers.gen.popups.DocumentosEditarFAPControllerGen;

public class DocumentosEditarFAPController extends DocumentosEditarFAPControllerGen {

	@javax.inject.Inject
	static GestorDocumentalService gestorDocumentalService;

	public static void index(String accion, Long idSolicitud, Long idDocumento) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/Documentacion/DocumentosEditarFAP.html");
		}

		SolicitudGenerica solicitud = DocumentosEditarFAPController.getSolicitudGenerica(idSolicitud);

		Documento documento = null;
		if ("crear".equals(accion))
			documento = DocumentosEditarFAPController.getDocumento();
		else if (!"borrado".equals(accion))
			documento = DocumentosEditarFAPController.getDocumento(idSolicitud, idDocumento);

		log.info("Visitando página: " + "fap/Documentacion/DocumentosEditarFAP.html");
		renderTemplate("fap/Documentacion/DocumentosEditarFAP.html", accion, idSolicitud, idDocumento, solicitud, documento);
	}

	//Borra el documento de la BD. Se borra también del AED si no está clasificado
	public static void borrar(Long idSolicitud, Long idDocumento) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Documento dbDocumento = DocumentosEditarFAPController.getDocumento(idSolicitud, idDocumento);
		SolicitudGenerica dbSolicitud = DocumentosEditarFAPController.getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			DocumentosEditarFAPController.borrarValidateRules(dbDocumento);
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

				dbSolicitud.documentacion.documentos.remove(dbDocumento);
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

			log.info("Acción Borrar de página: " + "gen/popups/DocumentosEditarFAP.html" + " , intentada con éxito" + " Agente: " + logAgente);
		} else {
			log.info("Acción Borrar de página: " + "gen/popups/DocumentosEditarFAP.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		}
		DocumentosEditarFAPController.borrarRender(idSolicitud, idDocumento);
	}
}
