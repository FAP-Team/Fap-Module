package controllers;

import messages.Messages;
import models.Documento;
import models.ResolucionFAP;
import play.mvc.Util;
import utils.DocumentosUtils;
import validation.CustomValidation;
import controllers.fap.AgenteController;
import controllers.gen.DocPortafirmaPorUriControllerGen;

public class DocPortafirmaPorUriController extends DocPortafirmaPorUriControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idResolucionFAP, Long idDocumento, Documento documento) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Documento dbDocumento = DocPortafirmaPorUriController.getDocumento(idResolucionFAP, idDocumento);

		DocPortafirmaPorUriController.DocPortafirmaPorUriBindReferences(documento);

		if (!Messages.hasErrors()) {

			DocPortafirmaPorUriController.DocPortafirmaPorUriValidateCopy("editar", dbDocumento, documento);

		}

		if (!Messages.hasErrors()) {
			DocPortafirmaPorUriController.editarValidateRules(dbDocumento, documento);
		}
		if (!Messages.hasErrors()) {
			ResolucionFAP resolucion = ResolucionFAP.findById(idResolucionFAP);
			dbDocumento.save();
			log.info("Acción Editar de página: " + "gen/DocPortafirmaPorUri/DocPortafirmaPorUri.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "gen/DocPortafirmaPorUri/DocPortafirmaPorUri.html" + " , intentada sin éxito (Problemas de Validación)");
		DocPortafirmaPorUriController.editarRender(idResolucionFAP, idDocumento);
	}
	
	
	public static void borrar(Long idResolucionFAP, Long idDocumento) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Documento dbDocumento = DocPortafirmaPorUriController.getDocumento(idResolucionFAP, idDocumento);
		ResolucionFAP dbResolucionFAP = DocPortafirmaPorUriController.getResolucionFAP(idResolucionFAP);
		if (!Messages.hasErrors()) {
			DocPortafirmaPorUriController.borrarValidateRules(dbDocumento);
		}

		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			dbResolucionFAP.docConsultaPortamfirmasResolucionPorUri.remove(dbDocumento);
			//Debe de borrarse tambien de los docs del portafirmas
			for (Documento uriDoc : dbResolucionFAP.docConsultaPortafirmasResolucion) {
				if(uriDoc.uri.equals(dbDocumento.uri)) {
					dbResolucionFAP.docConsultaPortafirmasResolucion.remove(uriDoc);  //Documento a borrar
					break;
				}
			}
			
			dbResolucionFAP.save();

			dbDocumento.delete();

			log.info("Acción Borrar de página: " + "gen/DocPortafirmaPorUri/DocPortafirmaPorUri.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else {
			log.info("Acción Borrar de página: " + "gen/DocPortafirmaPorUri/DocPortafirmaPorUri.html" + " , intentada sin éxito (Problemas de Validación)" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		}
		DocPortafirmaPorUriController.borrarRender(idResolucionFAP, idDocumento);
	}
	
	@Util
	public static Long crearLogica(Long idResolucionFAP, Documento documento) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Documento dbDocumento = DocPortafirmaPorUriController.getDocumento();
		ResolucionFAP dbResolucionFAP = DocPortafirmaPorUriController.getResolucionFAP(idResolucionFAP);

		DocPortafirmaPorUriController.DocPortafirmaPorUriBindReferences(documento);
		
		if (!Messages.hasErrors()) {

			DocPortafirmaPorUriController.DocPortafirmaPorUriValidateCopy("crear", dbDocumento, documento);

		}

		if (!Messages.hasErrors()) {
			DocPortafirmaPorUriController.crearValidateRules(dbDocumento, documento);
		}
		Long idDocumento = null;
		if (!Messages.hasErrors()) {
			dbDocumento.save();
			idDocumento = dbDocumento.id;
			//No añadirlo si ya está añadido:
			boolean añadido = false;
			for (Documento doc : dbResolucionFAP.docConsultaPortafirmasResolucion) {
				if (doc.uri.equals(dbDocumento.uri)){ //Ya añadido
					añadido = true;
				}
			}
			if(!añadido){
				dbResolucionFAP.docConsultaPortafirmasResolucion.add(dbDocumento); //Añadido para enviar
				dbResolucionFAP.docConsultaPortamfirmasResolucionPorUri.add(dbDocumento);
			}
			else
				Messages.error("Ya ha sido añadido un documento con esa uri");
			dbResolucionFAP.save();

			log.info("Acción Crear de página: " + "gen/DocPortafirmaPorUri/DocPortafirmaPorUri.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else {
			log.info("Acción Crear de página: " + "gen/DocPortafirmaPorUri/DocPortafirmaPorUri.html" + " , intentada sin éxito (Problemas de Validación)" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		}
		return idDocumento;
	}
	
	@Util
	public static void borrarRender(Long idResolucionFAP, Long idDocumento) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página borrada correctamente");
			Messages.keep();
			redirect("EditarResolucioncontroller.index", "editar", idResolucionFAP, idDocumento);
		}
		Messages.keep();
		redirect("DocPortafirmaPorUriController.index", "borrar", idResolucionFAP, idDocumento);
	}
	@Util
	public static void editarRender(Long idResolucionFAP, Long idDocumento) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("EditarResolucioncontroller.index", "editar", idResolucionFAP, idDocumento);
		}
		Messages.keep();
		redirect("DocPortafirmaPorUriController.index", "editar", idResolucionFAP, idDocumento);
	}


	@Util
	public static void DocPortafirmaPorUriValidateCopy(String accion, Documento dbDocumento, Documento documento) {
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
	
}
