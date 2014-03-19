package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import models.Documento;
import models.SolicitudGenerica;
import play.Logger;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import controllers.gen.ListaDocumentosExpedienteFAPControllerGen;



public class ListaDocumentosExpedienteFAPController extends ListaDocumentosExpedienteFAPControllerGen {
	
	@Inject
	private static GestorDocumentalService gestorDocService;
	
	public static void tabladocumentosExpediente(Long idSolicitud) {

		List<Documento> rows = new ArrayList<Documento>();
		
		//Obtenemos los documentos desde el gestor documental
		SolicitudGenerica solicitud = ListaDocumentosExpedienteFAPController.getSolicitudGenerica(idSolicitud);

		String expediente = solicitud.expedienteAed.idAed;
		Logger.warn(String.format("idAed del expediente: %s",  solicitud.expedienteAed.idAed));
		try {
			List<String> uris = gestorDocService.getDocumentosEnExpediente(expediente);
			for (String uri: uris) {
				Documento doc;
				doc = Documento.findByUri(uri);
				if (doc == null) {
					doc = new Documento();
					doc.uri = uri;
					doc.descripcion = gestorDocService.getDescripcionDocumento(uri);
					doc.tipo = gestorDocService.getTipoDocumento(uri);
				}
				
				rows.add(doc);
			}
		} catch (GestorDocumentalServiceException e) {
			e.printStackTrace();
			Logger.error("Excepción obteniendo los documentos del expediente " + expediente);
		}
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("tipo", "uri", "descripcion", "fechaSubida", "enlaceDescarga","id"));
	}


}
