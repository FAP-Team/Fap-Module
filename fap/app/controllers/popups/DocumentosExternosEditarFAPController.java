package controllers.popups;

import play.mvc.Util;
import validation.CustomValidation;
import messages.Messages;
import models.DocumentoExterno;
import models.SolicitudGenerica;
import controllers.gen.popups.DocumentosExternosEditarFAPControllerGen;

public class DocumentosExternosEditarFAPController extends DocumentosExternosEditarFAPControllerGen {

	public static void index(String accion, Long idSolicitud, Long idDocumentoExterno) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/popups/DocumentosExternosEditarFAP.html");
		}

		SolicitudGenerica solicitud = DocumentosExternosEditarFAPController.getSolicitudGenerica(idSolicitud);

		DocumentoExterno documentoExterno = null;
		if ("crear".equals(accion))
			documentoExterno = DocumentosExternosEditarFAPController.getDocumentoExterno();
		else if (!"borrado".equals(accion))
			documentoExterno = DocumentosExternosEditarFAPController.getDocumentoExterno(idSolicitud, idDocumentoExterno);

		log.info("Visitando página: " + "fap/Documentacion/DocumentosExternosEditarFAP.html");
		renderTemplate("fap/Documentacion/DocumentosExternosEditarFAP.html", accion, idSolicitud, idDocumentoExterno, solicitud, documentoExterno);
	}
	
	@Util
	public static void DocumentosExternosEditarFAPValidateCopy(String accion, DocumentoExterno dbDocumentoExterno, DocumentoExterno documentoExterno) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("documentoExterno", documentoExterno);
		CustomValidation.required("documentoExterno", documentoExterno);
		CustomValidation.required("documentoExterno.tipo", documentoExterno.tipo);
		CustomValidation.validValueFromTable("documentoExterno.tipo", documentoExterno.tipo);
		dbDocumentoExterno.tipo = documentoExterno.tipo;
		dbDocumentoExterno.descripcion = documentoExterno.descripcion;
		CustomValidation.required("documentoExterno.organo", documentoExterno.organo);
		dbDocumentoExterno.organo = documentoExterno.organo;
		CustomValidation.required("documentoExterno.expediente", documentoExterno.expediente);
		dbDocumentoExterno.expediente = documentoExterno.expediente;
		CustomValidation.required("documentoExterno.uri", documentoExterno.uri);
		dbDocumentoExterno.uri = documentoExterno.uri;

	}
	
}
