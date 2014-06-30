package controllers;

import play.mvc.Util;
import validation.CustomValidation;
import messages.Messages;
import models.DocumentoExterno;
import models.SolicitudGenerica;
import controllers.gen.DocumentosExternosFAPControllerGen;

import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import utils.BinaryResponse;

import javax.inject.Inject;

public class DocumentosExternosFAPController extends DocumentosExternosFAPControllerGen {

    @Inject
    static GestorDocumentalService gestorDocumentalService;

	public static void index(String accion, Long idSolicitud, Long idDocumentoExterno) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/DocumentosExternosFAP/DocumentosExternosFAP.html");
		}

		SolicitudGenerica solicitud = DocumentosExternosFAPController.getSolicitudGenerica(idSolicitud);

		DocumentoExterno documentoExterno = null;
		if ("crear".equals(accion))
			documentoExterno = DocumentosExternosFAPController.getDocumentoExterno();
		else if (!"borrado".equals(accion))
			documentoExterno = DocumentosExternosFAPController.getDocumentoExterno(idSolicitud, idDocumentoExterno);

		log.info("Visitando página: " + "fap/Documentacion/DocumentosExternosFAP.html");
		renderTemplate("fap/Documentacion/DocumentosExternosFAP.html", accion, idSolicitud, idDocumentoExterno, solicitud, documentoExterno);
	}
	
	@Util
	public static void DocumentosExternosFAPValidateCopy(String accion, DocumentoExterno dbDocumentoExterno, DocumentoExterno documentoExterno) {
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
		dbDocumentoExterno.uri = documentoExterno.uri;
        copiarHashDocumentoExterno(dbDocumentoExterno, documentoExterno);
	}

    private static void copiarHashDocumentoExterno(DocumentoExterno dbDocumentoExterno, DocumentoExterno documentoExterno) {
        if ((documentoExterno.uri != null) && !(documentoExterno.uri.isEmpty())) {
            try {
                BinaryResponse documentoAed = gestorDocumentalService.getDocumentoByUri(documentoExterno.uri);
                dbDocumentoExterno.hash = documentoAed.getPropiedades().getSellado().getHash();
            } catch (GestorDocumentalServiceException e) {
                e.printStackTrace();
               log.error("No se pudo recuperar el hash del documento con uri " + documentoExterno.uri);
            }
        } else {
            log.info("No se indicó una uri para el documento externo");
        }

	}
	
}
