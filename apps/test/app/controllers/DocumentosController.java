
package controllers;

import java.util.Map;

import play.mvc.Util;
import utils.GestorDocumentalUtils;
import validation.CustomValidation;
import messages.Messages;
import models.Documento;
import models.Solicitud;
import models.SolicitudGenerica;
import controllers.gen.DocumentosControllerGen;

public class DocumentosController extends DocumentosControllerGen {

	
	@Util
	public static Long crearLogica(Long idSolicitud, Documento documento, java.io.File fileAportacion) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		Documento dbDocumento = DocumentosController.getDocumento();
		Solicitud dbSolicitud = DocumentosController.getSolicitud(idSolicitud);
		DocumentosController.DocumentosBindReferences(documento, fileAportacion);

		if (!Messages.hasErrors()) {
			DocumentosController.DocumentosValidateCopy("crear", dbDocumento, documento, fileAportacion);
		}
		if (!Messages.hasErrors()) {
			DocumentosController.crearValidateRules(dbDocumento, documento, fileAportacion);
		}
		Long idDocumento = null;
		if (!Messages.hasErrors()) {
			dbDocumento.save();
			idDocumento = dbDocumento.id;
			dbSolicitud.documentacion.documentos.add(dbDocumento);
			dbSolicitud.save();
			log.info("Acción Crear de página: " + "gen/Documentos/Documentos.html" + " , intentada con éxito");
			
			// Guardamos los metadatos del documento
			if(!dbDocumento.sinMetadatos)
				controllers.fap.MetadatosFAPController.setMetadatos(dbDocumento.uri, dbDocumento.estadoElaboracion, dbDocumento.tipo);
		} 
		else {
			log.info("Acción Crear de página: " + "gen/Documentos/Documentos.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idDocumento;
	}

}
		