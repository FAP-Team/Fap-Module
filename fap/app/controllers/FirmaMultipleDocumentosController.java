package controllers;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Documento;
import models.ResolucionFAP;
import resolucion.ResolucionBase;
import services.FirmaService;
import services.GestorDocumentalService;
import utils.AedUtils;

import config.InjectorConfig;
import controllers.gen.FirmaMultipleDocumentosControllerGen;

public class FirmaMultipleDocumentosController extends FirmaMultipleDocumentosControllerGen {

	public static void firmar(Long id, List<Long> idsSeleccionados) {
		//Sobreescribir para incorporar funcionalidad
		//No olvide asignar los permisos
		//index();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Long idSolicitud = ids.get("idSolicitud");
		if (idsSeleccionados == null) {
			play.Logger.error("Se debe seleccionar al menos un documento para firmar");
			Messages.error("Se debe seleccionar al menos un documento para firmar");
			Messages.keep();
		} else {
			play.Logger.info("Se han seleccionado los documentos " + idsSeleccionados + " de la solicitud " + idSolicitud + " para firmar");
			GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
			FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
			for (int i = 0; i < idsSeleccionados.size(); i++) {
				Documento documento = Documento.find("select documento from Documento documento where documento.id=?", idsSeleccionados.get(i)).first();
				play.Logger.info("El documento " + documento.id + " se va a firmar");
				try {
					String firma = gestorDocumentalService.getDocumentoFirmaByUri(documento.uri);
					if (firma == null) {
						firma = firmaService.firmarDocumento(gestorDocumentalService.getDocumento(documento).getBytes());
						if (gestorDocumentalService.getDocumentoFirmaByUri(documento.uri) != null) {
							play.Logger.info("El documento " + documento.id + " se ha firmado correctamente (" + firma + ")");
						}
					} else {
						play.Logger.info("El documento " + documento.id + " ya está firmado");
						firma = firmaService.firmarDocumento(gestorDocumentalService.getDocumento(documento).getBytes());
					}
				} catch (Exception e) {
				}
			}

		}
		index("editar", idSolicitud);
	}
	
}
