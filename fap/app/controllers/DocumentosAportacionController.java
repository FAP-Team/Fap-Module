
package controllers;

import play.mvc.Util;
import utils.GestorDocumentalUtils;
import validation.CustomValidation;
import messages.Messages;
import models.Agente;
import models.Documento;
import models.SolicitudGenerica;
import controllers.fap.AgenteController;
import controllers.gen.DocumentosAportacionControllerGen;
			
public class DocumentosAportacionController extends DocumentosAportacionControllerGen {
	public static void index(String accion, Long idSolicitud, Long idDocumento) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/DocumentosAportacion/DocumentosAportacion.html");
		}

		SolicitudGenerica solicitud = DocumentosAportacionController.getSolicitudGenerica(idSolicitud);

		Documento documento = null;
		if ("crear".equals(accion)) {
			documento = DocumentosAportacionController.getDocumento();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				documento.save();
				idDocumento = documento.id;
				solicitud.aportaciones.actual.documentos.add(documento);
				solicitud.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			documento = DocumentosAportacionController.getDocumento(idSolicitud, idDocumento);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/DocumentosAportacion/DocumentosAportacion.html" + " Agente: " + logAgente);
		renderTemplate("fap/DocumentosAportacion/DocumentosAportacion.html", accion, idSolicitud, idDocumento, solicitud, documento);
	}
	
	@Util
	public static void DocumentosAportacionValidateCopy(String accion, Documento dbDocumento, Documento documento, java.io.File fileAportacion) {
		CustomValidation.clearValidadas();

		CustomValidation.required("documento", documento);
		dbDocumento.tipo = documento.tipo;
		dbDocumento.descripcion = documento.descripcion;

		if ((documento.uri != null) && (!documento.uri.isEmpty())) {
			services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
			try {
				gestorDocumentalService.duplicarDocumentoSubido(documento, dbDocumento);
			} catch (Exception e) {
				log.error("Ha habido un error al subir el documento " + e.getMessage());
				Messages.error("Ha habido un error al subir el documento");
				Messages.keep();
			}
		} else {
			int DESCMAXIMA = 255;
			if (documento.descripcion.length() > DESCMAXIMA) {
				validation.addError("documento.descripcion", "La descripción excede el tamaño máximo permitido de " + DESCMAXIMA + " caracteres");
			}
			if (fileAportacion == null)
				validation.addError("fileAportacion", "Archivo requerido");
			else if (fileAportacion.length() > properties.FapProperties.getLong("fap.file.maxsize"))
				validation.addError("fileAportacion", "Tamaño del archivo superior al máximo permitido (" + org.apache.commons.io.FileUtils.byteCountToDisplaySize(properties.FapProperties.getLong("fap.file.maxsize")) + ")");
			else {
				String extension = GestorDocumentalUtils.getExtension(fileAportacion);
				String mimeType = play.libs.MimeTypes.getMimeType(fileAportacion.getAbsolutePath());
				if (!utils.GestorDocumentalUtils.acceptExtension(extension))
					validation.addError("fileAportacion", "La extensión \"" + extension + "\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
				if (!utils.GestorDocumentalUtils.acceptMime(mimeType))
					validation.addError("fileAportacion", "El tipo mime \"" + mimeType + "\" del documento a incorporar, no es válido. Compruebe los formatos de documentos aceptados.");
			}
		}
		if (!validation.hasErrors()) {
			if (fileAportacion != null && documento.uri != null && documento.uri.isEmpty()) {
				try {
					services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
					gestorDocumentalService.saveDocumentoTemporal(dbDocumento, fileAportacion);
				} catch (services.GestorDocumentalServiceException e) {
					play.Logger.error(e, "Error al subir el documento al Gestor Documental");
					validation.addError("", "Error al subir el documento al Gestor Documental");
				} catch (Exception e) {
					play.Logger.error(e, "Ex: Error al subir el documento al Gestor Documental");
					validation.addError("", "Error al subir el documento al Gestor Documental");
				}
			}
		}
	}
}
		