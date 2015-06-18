package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;
import javax.transaction.TransactionManager;

import messages.Messages;
import models.Agente;
import models.ComunicacionInterna;
import models.Documento;
import models.ListaUris;
import models.RespuestaCIFap;
import models.SolicitudGenerica;
import play.db.jpa.JPA;
import play.mvc.Util;
import services.comunicacionesInternas.ComunicacionesInternasService;
import services.comunicacionesInternas.ComunicacionesInternasServiceException;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PaginaAltaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaCriteriaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;

public class PaginaAltaComunicacionInternaController extends PaginaAltaComunicacionInternaControllerGen {
	
	@Inject
	protected static ComunicacionesInternasService ciService;
	
	public static void index(String accion, Long idSolicitud, Long idComunicacionInterna) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html");
		}

		SolicitudGenerica solicitud = PaginaAltaComunicacionInternaController.getSolicitudGenerica(idSolicitud);

		ComunicacionInterna comunicacionInterna = null;
		if ("crear".equals(accion)) {
			comunicacionInterna = PaginaAltaComunicacionInternaController.getComunicacionInterna();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				comunicacionInterna.save();
				idComunicacionInterna = comunicacionInterna.id;
				solicitud.comunicacionesInternas.add(comunicacionInterna);
				solicitud.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			comunicacionInterna = PaginaAltaComunicacionInternaController.getComunicacionInterna(idSolicitud, idComunicacionInterna);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html" + " Agente: " + logAgente);
		renderTemplate("fap/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html", accion, idSolicitud, idComunicacionInterna, solicitud, comunicacionInterna);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmNuevoAsiento(Long idSolicitud, Long idComunicacionInterna, ComunicacionInterna comunicacionInterna, String btnNuevoAsiento) {
		checkAuthenticity();
		if (!permisoFrmNuevoAsiento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		ComunicacionInterna dbComunicacionInterna = PaginaAltaComunicacionInternaController.getComunicacionInterna(idSolicitud, idComunicacionInterna);
		PaginaAltaComunicacionInternaController.frmNuevoAsientoBindReferences(comunicacionInterna);

		if (!Messages.hasErrors()) {
			PaginaAltaComunicacionInternaController.frmNuevoAsientoValidateCopy("editar", dbComunicacionInterna, comunicacionInterna);
		}
		
		if (!Messages.hasErrors()) {
			dbComunicacionInterna.save();
		}

		if (!Messages.hasErrors()) {
			PaginaAltaComunicacionInternaController.frmNuevoAsientoValidateRules(dbComunicacionInterna, comunicacionInterna);
		}
		
		if (!Messages.hasErrors()) {
			try {
					RespuestaCIFap respuesta = null;
					if (dbComunicacionInterna.asiento.unidadOrganicaOrigen != null)
					   respuesta = ciService.crearNuevoAsientoAmpliado(dbComunicacionInterna.asiento);
					else
					   respuesta = ciService.crearNuevoAsiento(dbComunicacionInterna.asiento);
					
					if (respuesta != null) {
						if (dbComunicacionInterna.respuesta == null)
							dbComunicacionInterna.respuesta = new RespuestaCIFap();	
					
						if (respuesta.error == null) {
							dbComunicacionInterna.respuesta = respuesta;
						} else {
							log.error("No se ha podido crear el alta de la comunicación interna:" + respuesta.error.descripcion);
							Messages.error("No se ha podido crear el alta de la comunicación interna:" + respuesta.error.descripcion);
						}
					} else {
						log.error("No se ha podido crear el alta de la comunicación interna no se ha obtenido respuesta");
						Messages.error("No se ha podido crear el alta de la comunicación interna no se ha obtenido respuesta");
					}
			} catch (ComunicacionesInternasServiceException e) {
				log.error("No se ha podido crear el alta de la comunicación interna: " + e.getMessage());
				Messages.error("No se ha podido crear el alta de la comunicación interna");
			}
		}
		
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.enviada.name();
			dbComunicacionInterna.save();
			log.info("Acción Editar de página: " + "gen/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		
		PaginaAltaComunicacionInternaController.frmNuevoAsientoRender(idSolicitud, idComunicacionInterna);
	}

	@Util
	public static void frmNuevoAsientoRender(Long idSolicitud, Long idComunicacionInterna) {
		if (!Messages.hasErrors()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("ComunicacionesInternasController.index", ComunicacionesInternasController.getAccion(), idSolicitud, idComunicacionInterna);
		}
		Messages.keep();
		redirect("PaginaAltaComunicacionInternaController.index", "editar", idSolicitud, idComunicacionInterna);
	}
	
	@Util
	public static void frmNuevoAsientoValidateCopy(String accion, ComunicacionInterna dbComunicacionInterna, ComunicacionInterna comunicacionInterna) {
		CustomValidation.clearValidadas();

		CustomValidation.valid("comunicacionInterna.asiento", comunicacionInterna.asiento);
		CustomValidation.valid("comunicacionInterna", comunicacionInterna);
		CustomValidation.required("comunicacionInterna.asiento.userId", comunicacionInterna.asiento.userId);
		dbComunicacionInterna.asiento.userId = comunicacionInterna.asiento.userId;
		CustomValidation.required("comunicacionInterna.asiento.password", comunicacionInterna.asiento.password);
		dbComunicacionInterna.asiento.password = comunicacionInterna.asiento.password;
		dbComunicacionInterna.asiento.observaciones = comunicacionInterna.asiento.observaciones;
	}

	
	public static void tablatblDocAdicionales(Long idSolicitud, Long idComunicacionInterna) {

		java.util.List<ListaUris> uriDocPrincipal = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=?", idComunicacionInterna).fetch();
		java.util.List<Documento> rows = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.documentacion.documentos documento where solicitud.id=?", idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = new ArrayList<Documento>();
		
		for (Documento doc : rows){
			Boolean valido = true;
			if (doc.uri != null && doc.uri.equals(uriDocPrincipal.get(0).uri))
				valido = false;
			
			if (doc.uriPlatino != null && doc.uriPlatino.equals(uriDocPrincipal.get(0).uri))
				valido = false;
				
			if (valido)
				rowsFiltered.add(doc);
		}

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("descripcionVisible", "enlaceDescarga", "enlaceDescargaFirmado", "id"));
	}
	
	public static void seleccionar(Long id, List<Long> idsSeleccionados) {
		Long idComunicacionInterna = id;
		ComunicacionInterna comunicacionInterna = null;
		
		if (idComunicacionInterna != null)
		   comunicacionInterna = ComunicacionInterna.findById(idComunicacionInterna);
		
		if (comunicacionInterna != null) {
			List<ListaUris> lstUri = new ArrayList<ListaUris>();
			if (idsSeleccionados != null && !idsSeleccionados.isEmpty()){
				for (Long idDoc : idsSeleccionados) {
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
				}
			} 
		
			if (!Messages.hasErrors() && !lstUri.isEmpty()) {
				java.util.List<ListaUris> rows = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=?", idComunicacionInterna).fetch();
				ListaUris lstUriDocPrincipal = null;
				if (rows != null && !rows.isEmpty()){
					lstUriDocPrincipal = rows.get(0);
					lstUri.add(0, lstUriDocPrincipal);
					comunicacionInterna.asiento.uris = lstUri;
					comunicacionInterna.asiento.numeroDocumentos = lstUri.size();
					comunicacionInterna.estado = EstadosComunicacionInternaEnum.docAdjuntos.name();
					comunicacionInterna.save(); 
				} else {
					log.error("No se encuentra el documento principal de la comunicación");
					Messages.error("No se encuentra el documento principal de la comunicación");
				}
			} 
			
		} else {
			log.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
			Messages.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
		}
			
		SolicitudGenerica solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.comunicacionesInternas comunicacionesInternas where comunicacionesInternas.id = ?", idComunicacionInterna).first();
		redirect("PaginaAltaComunicacionInternaController.index", "editar", solicitud.id, idComunicacionInterna);
	}
	
	public static void tabladocumentosCI(Long idComunicacionInterna) {

		java.util.List<ListaUris> rows = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=?", idComunicacionInterna).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = new ArrayList<Documento>();
		
		for (ListaUris lsturi : rows){
			Documento doc = null;
			if (lsturi != null && lsturi.uri != null && !lsturi.uri.isEmpty()) {
				doc = Documento.findByUri(lsturi.uri);
				
				if (doc == null){
					doc = Documento.findByUriPlatino(lsturi.uri);
				}
				
				if (doc != null)
					rowsFiltered.add(doc);
			}
			
		}

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("descripcionVisible", "enlaceDescarga", "enlaceDescargaFirmado", "id"));
	}
	
}
