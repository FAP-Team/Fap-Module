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
	
	public static void tablatblDocPrincipal(Long idSolicitud) {

		java.util.List<Documento> rows = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.documentacion.documentos documento where solicitud.id=?", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		List<Documento> rowsFiltered = new ArrayList<Documento>();
		for (Documento doc : rows){
			if (doc.firmado)
				rowsFiltered.add(doc);
		}
		
		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("descripcionVisible", "enlaceDescarga", "enlaceDescargaFirmado", "id"));
	}
	
	public static void tablatblDocPrincipalSelect(Long idComunicacionInterna) {

		java.util.List<ListaUris> rows = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=?", idComunicacionInterna).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = new ArrayList<Documento>();
		for (ListaUris lsturi: rows){
			Documento doc = null;
			if (lsturi.uri != null && !lsturi.uri.isEmpty()) {
				 doc = Documento.findByUri(lsturi.uri);
				 
				 if (doc != null)
					 rowsFiltered.add(doc);
			}
		}
		
		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("descripcionVisible", "id"));
	}
	
	public static void seleccionar(Long id, List<Long> idsSeleccionados) {
		Long idComunicacionInterna = id;
		ComunicacionInterna comunicacionInterna = null;
		
		if (idComunicacionInterna != null)
		   comunicacionInterna = ComunicacionInterna.findById(idComunicacionInterna);
		
		if (comunicacionInterna != null) {
			List<ListaUris> lstUri = new ArrayList<ListaUris>();
			if (idsSeleccionados != null && !idsSeleccionados.isEmpty() && idsSeleccionados.size() == 1){
				Long idDoc = idsSeleccionados.get(0);
				Documento doc = Documento.findById(idDoc);
				if (doc != null){
					ListaUris uri = new ListaUris();
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
			} else {
				log.info("Un documento como máximo");
				Messages.info("Se debe escoger un documento principal como máximo");
			}
		
			if (!Messages.hasMessages() && !lstUri.isEmpty()) {
				comunicacionInterna.asiento.uris = lstUri;
				comunicacionInterna.asiento.numeroDocumentos = lstUri.size();
				comunicacionInterna.estado = EstadosComunicacionInternaEnum.docAdjuntos.name();
				comunicacionInterna.save(); 
			} 
			
		} else {
			log.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
			Messages.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
		}
			
		
		if (!Messages.hasMessages()){
			SolicitudGenerica solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.comunicacionesInternas comunicacionesInternas where comunicacionesInternas.id = ?", idComunicacionInterna).first();
			redirect("PaginaAltaComunicacionInternaController.index", "editar", solicitud.id, idComunicacionInterna);
		} else {
			SolicitudGenerica solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.comunicacionesInternas comunicacionesInternas where comunicacionesInternas.id = ?", idComunicacionInterna).first();
			Messages.keep();
			redirect("PaginaNuevaComunicacionInternaDocumentosController.index", "editar", solicitud.id, idComunicacionInterna);
		}
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmNoAportarDocumentacion(Long idSolicitud, Long idComunicacionInterna, String btnNuevaComunicacionInternaDocumentos) {
		checkAuthenticity();
		if (!permisoFrmNoAportarDocumentacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			ComunicacionInterna comunicacionInterna = getComunicacionInterna(idSolicitud, idComunicacionInterna);
			if( comunicacionInterna != null && comunicacionInterna.asiento != null){
				comunicacionInterna.asiento.uris = null;
				comunicacionInterna.save();
			}
		}

		if (!Messages.hasErrors()) {
			PaginaNuevaComunicacionInternaDocumentosController.frmNoAportarDocumentacionValidateRules();
		}
		
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaNuevaComunicacionInternaDocumentos/PaginaNuevaComunicacionInternaDocumentos.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNuevaComunicacionInternaDocumentos/PaginaNuevaComunicacionInternaDocumentos.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		PaginaNuevaComunicacionInternaDocumentosController.frmNoAportarDocumentacionRender(idSolicitud, idComunicacionInterna);
	}

	@Util
	public static void frmNoAportarDocumentacionRender(Long idSolicitud, Long idComunicacionInterna) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PaginaAltaComunicacionInternaController.index", "editar", idSolicitud, idComunicacionInterna);
		}
		Messages.keep();
		redirect("PaginaAltaComunicacionInternaController.index", "editar", idSolicitud, idComunicacionInterna);
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
