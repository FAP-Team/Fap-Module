
package controllers.popups;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import aed.TiposDocumentosClient;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;

import play.db.jpa.JPAPlugin;
import properties.FapProperties;

import tags.ComboItem;
import controllers.gen.popups.PopUpDocTiposControllerGen;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;
			
public class PopUpDocTiposController extends PopUpDocTiposControllerGen {
	
	public static List<ComboItem> documento_tipo() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		Map <String, Long> parametrosUrl = (Map<String, Long>)tags.TagMapStack.top("idParams");
		SolicitudGenerica solicitud = getSolicitudGenerica(parametrosUrl.get("idSolicitud"));
		List <TipoDocumento> tiposDocumentos = TiposDocumentosClient.getListTiposDocumentosAportadosCiudadano (solicitud.verificacion.tramiteNombre);
		for (TipoDocumento tDoc: tiposDocumentos){
			result.add(new ComboItem(tDoc.getUri(), tDoc.getDescripcion()));
		}
		return result;
	}
	
	public static void editar(Long idSolicitud, Long idDocumento, Documento documento) {
		checkAuthenticity();
		if (!permiso("update")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		Documento dbDocumento = null;
		SolicitudGenerica solicitud = null;
		if (!Messages.hasErrors()) {
			dbDocumento = getDocumento(idSolicitud, idDocumento);
			solicitud = getSolicitudGenerica(idSolicitud);
		}

		if (!Messages.hasErrors()) {
			PopUpDocTiposValidateCopy(dbDocumento, documento);
		}

		if (!Messages.hasErrors()) {
			dbDocumento.save();
			solicitud.verificacion.fechaUltimaActualizacion = new DateTime();
		}

		if (!Messages.hasErrors()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		} else {
			Messages.keep();
			abrir("editar", idDocumento, idSolicitud);
		}

	}

}
		