package controllers;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Firmante;
import models.SolicitudGenerica;
import play.mvc.Util;
import services.RegistroServiceException;
import tramitacion.TramiteRenuncia;
import controllers.gen.RenunciaPresentarControllerGen;

public class RenunciaPresentarController extends RenunciaPresentarControllerGen {
	
	/**
	 * 
	 * @param idSolicitud
	 * @param firma
	 * @param firmarRegistrarNif
	 * 
	 */
	public static void formFirma(Long idSolicitud, String firma, String firmarRegistrarNif) {
		checkAuthenticity();
		if (!permisoFormFirma("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = RenunciaPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteRenuncia trRenuncia = new TramiteRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trRenuncia.firmar(firma);
			if (!Messages.hasErrors()) {
				try {
					trRenuncia.registrar();
				} catch (RegistroServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (!Messages.hasErrors()) {
			RenunciaPresentarController.formFirmaValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/RenunciaPresentar/RenunciaPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/RenunciaPresentar/RenunciaPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		RenunciaPresentarController.formFirmaRender(idSolicitud);
	}
	
	public static void tablatablaFirmantesHecho(Long idSolicitud) {
		// Filtra los firmantes para mostrar los que ya han firmado
		java.util.List<Firmante> rows = Firmante.find("select firmante from SolicitudGenerica solicitud join solicitud.renuncia.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is not null", idSolicitud, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "fechaFirma", "id"));
	}

	public static void tablatablaFirmantesEspera(Long idSolicitud) {
		//Filtra los firmantes para mostrar los que no tienen fecha de firma
		java.util.List<Firmante> rows = Firmante.find("select firmante from SolicitudGenerica solicitud join solicitud.renuncia.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is null", idSolicitud, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		
		renderJSON(response.toJSON("idvalor", "nombre", "id"));
	}
}
