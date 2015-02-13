package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;

import messages.Messages;
import messages.Messages.MessageType;
import models.Documento;
import models.Firma;
import models.Firmante;
import models.Firmantes;
import models.SolicitudGenerica;
import platino.FirmaUtils;
import play.db.jpa.JPA;
import play.mvc.Util;
import play.mvc.Scope.Session;
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

		Map<String, Object> json = new HashMap<String, Object>();
		ArrayList<String> errores = new ArrayList<String>();
		ArrayList<String> aciertos = new ArrayList<String>();
		Documento documento = null;
		SolicitudGenerica solicitud = null;

		EntityTransaction tx = JPA.em().getTransaction();
		try {
			if (tx.isActive())
		    	tx.commit();
		    tx.begin();
		    
			documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
			solicitud = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud join solicitud.documentacion.documentos documento where documento.id=?", idDocumento).first();
			if (documento != null) {
				Messages.clear();
	
				String sessionid = Session.current().getId();
				String sessionuser = null;
				if (Session.current().contains("username"))
					sessionuser = Session.current().get("username");
				play.Logger.info("Identificador de sesion: " + sessionid + " usuario de sesion: " + sessionuser);
				play.Logger.info("Firmando documento " + documento.uri + " de la Solicitud " + solicitud.id );
	
				json.put("idDocumento", idDocumento);
				json.put("firmado", false);
	
				if (documento.firmantes == null) {
					documento.firmantes = new Firmantes();
					documento.save();
				}
	
				//Calcula los firmantes del documento
				play.Logger.info("Calculando firmantes del documento " + documento.uri + " de la Solicitud " + solicitud.id);
				documento.firmantes.todos = calcularFirmantesdocumentos(solicitud.id);
				documento.firmantes.save();
	
				FirmaUtils.firmarDocumento(documento, documento.firmantes.todos, firma, null);
	
				if (!Messages.hasErrors()) {
					play.Logger.info("Firma de documento " + documento.uri + " con éxito");
					if (documento.firmantes.todos.size() > 0 && FirmaUtils.hanFirmadoTodos(documento.firmantes.todos))
						json.put("firmado", true);
					else
						json.put("firmado", false);
				}
	
			} else {
				String errorDocumento = "";
				String errorSolicitud = "";
				if (documento == null) {
					errorDocumento = "Error al obtener el documento " + idDocumento;
					errores.add(errorDocumento);
				}
				if (solicitud == null) {
					errorSolicitud = "Error al obtener la solicitud";
					errores.add(errorSolicitud);
				}
			}
			
			tx.commit();
	 	}catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    play.Logger.error("Se ha producido un error en el proceso de firma: " + documento.descripcion + " de la solicitud: " + solicitud.id);
		    Messages.error("Error en el proceso de firma");
		    e.printStackTrace();
		}
		tx.begin();

		for (String mensaje : Messages.messages(MessageType.ERROR)) {
			errores.add(mensaje);
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
