package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.mvc.Util;

import com.google.inject.spi.Message;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.ComunicacionInterna;
import models.Documento;
import models.ListaUris;
import models.SolicitudGenerica;
import controllers.fap.AgenteController;
import controllers.gen.PaginaNuevaComunicacionInternaDocumentosControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PaginaNuevaComunicacionInternaDocumentosController extends PaginaNuevaComunicacionInternaDocumentosControllerGen {

	public static void index(String accion, Long idSolicitud, Long idComunicacionInterna) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/PaginaNuevaComunicacionInternaDocumentos/PaginaNuevaComunicacionInternaDocumentos.html");
		}

		SolicitudGenerica solicitud = PaginaNuevaComunicacionInternaDocumentosController.getSolicitudGenerica(idSolicitud);

		ComunicacionInterna comunicacionInterna = null;
		if ("crear".equals(accion)) {
			comunicacionInterna = PaginaNuevaComunicacionInternaDocumentosController.getComunicacionInterna();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				comunicacionInterna.save();
				idComunicacionInterna = comunicacionInterna.id;
				solicitud.comunicacionesInternas.add(comunicacionInterna);
				solicitud.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			comunicacionInterna = PaginaNuevaComunicacionInternaDocumentosController.getComunicacionInterna(idSolicitud, idComunicacionInterna);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/PaginaNuevaComunicacionInternaDocumentos/PaginaNuevaComunicacionInternaDocumentos.html" + " Agente: " + logAgente);
		renderTemplate("fap/PaginaNuevaComunicacionInternaDocumentos/PaginaNuevaComunicacionInternaDocumentos.html", accion, idSolicitud, idComunicacionInterna, solicitud, comunicacionInterna);
	}
	
	public static void seleccionar(Long id, List<Long> idsSeleccionados) {
		Long idComunicacionInterna = id;
		ComunicacionInterna comunicacionInterna = ComunicacionInterna.findById(idComunicacionInterna);
		
		if (comunicacionInterna != null) {
			List<ListaUris> lstUri = new ArrayList<ListaUris>();
			if (idsSeleccionados != null && !idsSeleccionados.isEmpty()){
				for (Long idDoc : idsSeleccionados) {
					Documento doc = Documento.findById(idDoc);
					ListaUris uri = new ListaUris();
					if (doc != null){
						if (doc.uriPlatino != null && !doc.uriPlatino.isEmpty()){
							uri.uri = doc.uriPlatino;
							lstUri.add(uri);
						} else
							if (doc.uri != null && !doc.uri.isEmpty()){
								uri.uri = doc.uri;
								lstUri.add(uri);
							}
					} else {
						log.error("No se encuentra el documento con identificador: " + idDoc);
						Messages.error("No se encuentra el documento con identificador: " + idDoc);
					}
				}
			}
			
			if (!lstUri.isEmpty()) {
				comunicacionInterna.asiento.uris = lstUri;
				comunicacionInterna.asiento.numeroDocumentos = lstUri.size();
				comunicacionInterna.estado = EstadosComunicacionInternaEnum.docAdjuntos.name();
				comunicacionInterna.save(); 
			} else {
				log.error("Debe seleccionar al menos un documento");
				Messages.error("Debe seleccionar al menos un documento");
			}
			
		} else {
			log.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
			Messages.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
		}
			
		
		if (!Messages.hasErrors()){
			SolicitudGenerica solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.comunicacionesInternas comunicacionesInternas where comunicacionesInternas.id = ?", idComunicacionInterna).first();
			Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
			ids.put("idSolicitud", solicitud.id);
			redirect("PaginaAltaComunicacionInternaController.index", "editar", solicitud.id, idComunicacionInterna);
		} else {
			SolicitudGenerica solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.comunicacionesInternas comunicacionesInternas where comunicacionesInternas.id = ?", idComunicacionInterna).first();
			Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
			ids.put("idSolicitud", solicitud.id);
			Messages.keep();
			redirect("PaginaNuevaComunicacionInternaDocumentosController.index", "editar", solicitud.id, idComunicacionInterna);
		}
	}
	
	@Util
	public static ComunicacionInterna getComunicacionInterna(Long idSolicitud, Long idComunicacionInterna) {
		ComunicacionInterna comunicacionInterna = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}

		if (idComunicacionInterna == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idComunicacionInterna"))
				Messages.fatal("Falta parámetro idComunicacionInterna");
		}
		if (idSolicitud != null && idComunicacionInterna != null) {
			//No se asocia la entidad a la solicitud hasta que se rellena el asiento
			comunicacionInterna = ComunicacionInterna.findById(idComunicacionInterna);
			if (comunicacionInterna == null)
				Messages.fatal("Error al recuperar ComunicacionInterna");
		}
		return comunicacionInterna;
	}
}
