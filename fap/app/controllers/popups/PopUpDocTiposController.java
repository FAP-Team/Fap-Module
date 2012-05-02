
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
import play.mvc.Util;
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
			solicitud.save();
		}

		if (!Messages.hasErrors()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		} else {
			Messages.keep();
			abrir("editar", idDocumento, idSolicitud);
		}

	}
	
	public static void abrir(String accion,Long idDocumento,Long idSolicitud){
		Documento documento;
		if(accion.equals("crear")){
            documento = new Documento();
			
		}else{
		    documento = getDocumento(idSolicitud, idDocumento);
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "PopUpDocTiposControllerGen");
		renderTemplate("gen/popups/PopUpDocTipos.html",accion,idDocumento,documento,idSolicitud);
	}
	
	@Util
    protected static Documento getDocumento(Long idSolicitud, Long idDocumento){
        Documento documento = null;
        if(idSolicitud == null){
            Messages.fatal("Falta parámetro idSolicitud");
        }else if(idDocumento == null){
            Messages.fatal("Falta parámetro idDocumento");
        }else{
            documento = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.documentacion.documentos documento where solicitud.id=? and documento.id=?", idSolicitud, idDocumento).first();
            if(documento == null){
            	documento = Documento.findById(idDocumento);
            	if(documento == null)
            		Messages.fatal("Error al recuperar Documento");
            }
        }
        return documento;
    }

}
		