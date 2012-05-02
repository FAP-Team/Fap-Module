
package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import tags.ComboItem;
import aed.TiposDocumentosClient;
import controllers.gen.popups.PopUpDocNuevosTiposControllerGen;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
			
public class PopUpDocNuevosTiposController extends PopUpDocNuevosTiposControllerGen {
	
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
			PopUpDocNuevosTiposValidateCopy(dbDocumento, documento);
		}

		if (!Messages.hasErrors()) {
			solicitud.verificacion.fechaUltimaActualizacion = new DateTime();
			dbDocumento.save();
		}

		if (!Messages.hasErrors()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		} else {
			Messages.keep();
			abrir("editar", idDocumento, idSolicitud);
		}

	}
	
	protected static Documento getDocumento(Long idSolicitud, Long idDocumento){
        Documento documento = null;
        if(idSolicitud == null){
            Messages.fatal("Falta parámetro idSolicitud");
        }else if(idDocumento == null){
            Messages.fatal("Falta parámetro idDocumento");
        }else{
            documento = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.verificacion.nuevosDocumentos documento where solicitud.id=? and documento.id=?", idSolicitud, idDocumento).first();
            if(documento == null){
            	documento = Documento.findById(idDocumento);
            	if(documento == null)
            		Messages.fatal("Error al recuperar Documento");
            }
        }
        return documento;
    }

}
		