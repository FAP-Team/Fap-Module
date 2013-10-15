package controllers;

import messages.Messages;
import models.Documento;
import play.mvc.Util;
import utils.DocumentosUtils;
import utils.GestorDocumentalUtils;
import validation.CustomValidation;
import controllers.gen.DocConsultaPortafirmaCrearControllerGen;

public class DocConsultaPortafirmaCrearController extends DocConsultaPortafirmaCrearControllerGen {

	@Util
	public static void DocConsultaPortafirmaCrearValidateCopy(String accion, Documento dbDocumento, Documento documento, java.io.File file) {
		CustomValidation.clearValidadas();

		 if ((documento.uri != null) && (!documento.uri.isEmpty())){ //Poniendo documento por uri
				CustomValidation.clearValidadas();
				CustomValidation.valid("documento", documento);
				CustomValidation.validValueFromTable("documento.tipo", documento.tipo);
				if (DocumentosUtils.docExisteEnAed(documento.uri)){
					dbDocumento.tipo = DocumentosUtils.getTipoDocumento(documento.uri);
					dbDocumento.descripcion = DocumentosUtils.getDescripcionVisible(documento.uri);
					dbDocumento.descripcionVisible = DocumentosUtils.getDescripcionVisible(documento.uri);
					dbDocumento.uri = documento.uri;	
				} 
				else{
					play.Logger.error("El documento con uri "+documento.uri+" no existe");
					Messages.error("Error no existe el documento con uri "+documento.uri);
				}
			}
		 else if ((documento.uri == null) || (documento.uri.isEmpty())){
			CustomValidation.required("documento", documento);
			dbDocumento.tipo = documento.tipo;
			dbDocumento.descripcion = documento.descripcion;
	
			int DESCMAXIMA = 255;
			if (documento.descripcion.length() > DESCMAXIMA) {
				validation.addError("documento.descripcion", "La descripción excede el tamaño máximo permitido de " + DESCMAXIMA + " caracteres");
			}
			if (file == null)
				validation.addError("file", "Archivo requerido");
			else if (file.length() > properties.FapProperties.getLong("fap.file.maxsize"))
				validation.addError("file", "Tamaño del archivo superior al máximo permitido (" + org.apache.commons.io.FileUtils.byteCountToDisplaySize(properties.FapProperties.getLong("fap.file.maxsize")) + ")");
			else {
				String extension = GestorDocumentalUtils.getExtension(file);
				String mimeType = play.libs.MimeTypes.getMimeType(file.getAbsolutePath());
				if (!utils.GestorDocumentalUtils.acceptExtension(extension))
					validation.addError("file", "La extensión \"" + extension + "\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
				if (!utils.GestorDocumentalUtils.acceptMime(mimeType))
					validation.addError("file", "El tipo mime \"" + mimeType + "\" del documento a incorporar, no es válido. Compruebe los formatos de documentos aceptados.");
			}
			CustomValidation.valid("documento", documento);
			if (!documento.uri.isEmpty()){
				dbDocumento.uri = documento.uri;
			}else{
				dbDocumento.uri = null;
			}
			
			if (!validation.hasErrors()) {
				if (file != null) {
					try {
						services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
						gestorDocumentalService.saveDocumentoTemporal(dbDocumento, file);
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
	
}
