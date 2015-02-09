package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import messages.Messages;
import messages.Messages.MessageType;
import models.Documento;
import models.Firma;
import models.Firmante;
import models.Firmantes;
import models.SolicitudGenerica;
import platino.FirmaUtils;
import play.mvc.Util;
import reports.Report;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;

import com.google.gson.Gson;

import controllers.gen.DocumentacionFAPControllerGen;

public class DocumentacionFAPController extends DocumentacionFAPControllerGen {

	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/DocumentacionFAP/DocumentacionFAP.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = DocumentacionFAPController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = DocumentacionFAPController.getSolicitudGenerica(idSolicitud);

		log.info("Visitando página: " + "fap/DocumentacionFAP/DocumentacionFAP.html");
		renderTemplate("gen/DocumentacionFAP/DocumentacionFAP.html", accion, idSolicitud, solicitud);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formAbrirPlantillaFH(Long idSolicitud, String btnAbrirPlantillaFH) {
		checkAuthenticity();
		if (!permisoFormAbrirPlantillaFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			try {
				SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
				new Report("reports/permitirFirmaFH.html").header("reports/header.html").renderResponse(solicitud);
			} catch (Exception e) {
				play.Logger.error("Error generando la plantilla de habilitar funcionario a firmar", e.getMessage());
				Messages.error("Error generando la plantilla de habilitar funcionario a firmar");
			}
		}

		if (!Messages.hasErrors()) {
			DocumentacionFAPController.formAbrirPlantillaFHValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/DocumentacionFAP/DocumentacionFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/DocumentacionFAP/DocumentacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		DocumentacionFAPController.formAbrirPlantillaFHRender(idSolicitud);
	}

	@Util
	public static String firmardocumentos(Long idDocumento, String firma) {

		Documento documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
		Map<String, Object> json = new HashMap<String, Object>();
		ArrayList<String> errores = new ArrayList<String>();
		ArrayList<String> aciertos = new ArrayList<String>();

		if (documento != null) {

			Messages.clear();

			play.Logger.info("Firmando documento " + documento.uri);

			Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
			Map<String, Object> vars = new HashMap<String, Object>();
			json.put("idDocumento", idDocumento);
			json.put("firmado", false);
			if (secure.checkAcceso("editarFirmaDocumento", "editar", ids, vars)) {
				if (documento.firmantes == null) {
					documento.firmantes = new Firmantes();
					documento.save();
				}
				if (documento.firmantes.todos == null || documento.firmantes.todos.size() == 0) {
					Long idSolicitud = ids.get("idSolicitud");
					documento.firmantes.todos = calcularFirmantesdocumentos(idSolicitud);
					documento.firmantes.save();
				}
				FirmaUtils.firmarDocumento(documento, documento.firmantes.todos, firma, null);
			} else {
				//ERROR
				String error = "No tiene permisos suficientes para firmar el documento " + documento.descripcion;
				Messages.error(error);
				errores.add(error);
			}

			if (!Messages.hasErrors()) {
				play.Logger.info("Firma de documento " + documento.uri + " con éxito");
				if (documento.firmantes.todos.size() > 0 && FirmaUtils.hanFirmadoTodos(documento.firmantes.todos))
					json.put("firmado", true);
				else
					json.put("firmado", false);
			}

		} else {
			String error = "Error al obtener el documento " + idDocumento;
			play.Logger.info(error);
			errores.add(error);
		}

		for (String mensaje : Messages.messages(MessageType.ERROR)) {
			//Comentado porque está duplicando los mensajes de error
//			errores.add(mensaje);
		}

		for (String mensaje : Messages.messages(MessageType.OK)) {
			aciertos.add(mensaje);
		}

		json.put("errores", errores);
		json.put("aciertos", aciertos);
		return new Gson().toJson(json);
	}

	@javax.inject.Inject
	static GestorDocumentalService gestorDocumentalService;

	public static String obtenerFirmadoDocumentodocumentos(Long idDocumento) {
		if (!permiso("leer")) {
			HashMap error = new HashMap();
			error.put("error", "No tiene permisos suficientes");
			return new Gson().toJson(error);
		}
		Documento documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
		if (documento != null) {
			play.Logger.info("El documento " + documento.id + " tiene la uri " + documento.uri + " y  firmado a " + documento.firmado);
			HashMap json = new HashMap();

			if (FirmaUtils.hanFirmadoTodos(documento.firmantes.todos)) {
				json.put("firmado", true);
				json.put("descripcion", documento.descripcion);
				json.put("refAed", documento.refAed);
				return new Gson().toJson(json);
			} else {
				List<String> firmantes = new ArrayList<String>();
				for (Firmante firmante : documento.firmantes.todos) {
					firmantes.add(firmante.idvalor);
				}
				Firma firma = null;
				try {
					firma = gestorDocumentalService.getFirma(documento);
				} catch (GestorDocumentalServiceException e) {
					e.printStackTrace();
				}
				json.put("id", documento.id);
				json.put("firmado", false);
				if (firma != null) {
					json.put("firma", firma.getContenido());
				}
				json.put("refAed", documento.refAed);
				json.put("descripcion", documento.descripcion);
				json.put("firmantes", firmantes);
				json.put("url", FirmaUtils.obtenerUrlDocumento(documento.id));
				return new Gson().toJson(json);
			}
		}
		play.Logger.info("Error al obtener el documento " + idDocumento);
		return null;
	}


}
