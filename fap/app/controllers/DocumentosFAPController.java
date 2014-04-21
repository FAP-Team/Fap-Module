package controllers;

import java.util.List;

import messages.Messages;
import models.Documento;
import models.Metadato;
import models.SolicitudGenerica;
import play.mvc.Util;
import controllers.gen.DocumentosFAPControllerGen;

public class DocumentosFAPController extends DocumentosFAPControllerGen {
	public static void index(String accion, Long idSolicitud, Long idDocumento, List<Metadato> metadatos) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/Documentacion/DocumentosFAP.html");
		}

		SolicitudGenerica solicitud = DocumentosFAPController.getSolicitudGenerica(idSolicitud);

		Documento documento = null;
		if ("crear".equals(accion))
			documento = DocumentosFAPController.getDocumento();
		else if (!"borrado".equals(accion))
			documento = DocumentosFAPController.getDocumento(idSolicitud, idDocumento);

		log.info("Visitando página: " + "fap/Documentacion/DocumentosFAP.html");
		renderTemplate("fap/Documentacion/DocumentosFAP.html", accion, idSolicitud, idDocumento, solicitud, documento, metadatos);
	}
	

	@Util
	public static Long crearLogica(Long idSolicitud, Documento documento, java.io.File fileAportacion, List<Metadato> metadatos) {
		Long idDocumento = null;
		idDocumento = DocumentosFAPControllerGen.crearLogica(idSolicitud, documento, fileAportacion, metadatos);
		if (idDocumento == null) {
			play.cache.Cache.add("metadatos", metadatos, "2mn" );
		} else {
			play.cache.Cache.delete("metadatos");
		}
		return idDocumento;
		
	}
}
