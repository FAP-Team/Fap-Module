package controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.DocumentoNotificacion;
import models.Notificacion;
import play.mvc.Util;
import properties.FapProperties;
import services.NotificacionService;
import tables.TableRenderResponse;
import utils.AedUtils;
import utils.NotificacionUtils;
import controllers.fap.AgenteController;
import controllers.gen.NotificacionVerControllerGen;
import es.gobcan.aciisi.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;

public class NotificacionVerController extends NotificacionVerControllerGen {
	
	@Inject
    private static NotificacionService notificacionService;
	
	public static void index(String accion, Long idNotificacion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/NotificacionVer/NotificacionVer.html");
		}
		checkRedirigir();

		Notificacion notificacion = null;
		if ("crear".equals(accion))
			notificacion = NotificacionVerController.getNotificacion();
		else if (!"borrado".equals(accion))
			notificacion = NotificacionVerController.getNotificacion(idNotificacion);

		log.info("Visitando página: " + "fap/Notificacion/NotificacionVer.html");
		String url = FapProperties.get("fap.notificacion.enlaceSede");
		String urlRequerimiento = null;
		if ((notificacion.documentosANotificar != null) && (!notificacion.documentosANotificar.isEmpty())){
			urlRequerimiento = notificacion.documentosANotificar.get(0).urlDescarga;
		}
		renderTemplate("fap/Notificacion/NotificacionVer.html", accion, idNotificacion, notificacion, url, urlRequerimiento);
	}
	
	//En la notificación sólo hay 5 tipos de documentos -> 5 documentos que podemos verlos en la misma tabla.
	
	
	public static void tablalistaDocumentosNotificados(Long idNotificacion) {
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		Notificacion notificacion = Notificacion.findById(idNotificacion);
		List<Documento> rowsDocumentos = new ArrayList<Documento>();
		String uriDocAux = "";
		
		//Obtener documento de puestaADisposicion
		if(notificacion.documentoPuestaADisposicion != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.PUESTA_A_DISPOSICION);
			if (!uriDocAux.equals("")){
				Documento doc = Documento.findByUri(uriDocAux);
				Agente agente = AgenteController.getAgente();
				if ((agente.rolActivo.equals("usuario") && (notificacion.estado.contains("leida") || notificacion.estado.contains("leidaplazorespuestavencido")
						|| notificacion.estado.contains("respondida"))) || (!agente.rolActivo.equals("usuario")))
					rowsDocumentos.add(doc);
			}
		}
		
		//Obtener documento de acuseDeRecibo
		if (notificacion.documentoAcuseRecibo != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.ACUSE_RECIBO);
			if (!uriDocAux.equals("")){
				Documento doc;
				doc = Documento.findByUri(uriDocAux);
				if (doc == null) {
					doc = new Documento();
					doc.uri = uriDocAux;
					doc.firmado = notificacionService.obtenerFirmadoDocumentoNotificacion("", notificacion.uri, DocumentoNotificacionEnumType.ACUSE_RECIBO);					
					try {	
						doc.descripcion = NotificacionUtils.obtenerDescripcionDocumento(uriDocAux);
						doc.tipo = NotificacionUtils.obtenerTipoDocumento(uriDocAux);
					} catch (Exception ex) {
						play.Logger.error("Excepción obteniendo la descripción y el tipo de documento de la uri: " + uriDocAux + "Error: " + ex.getMessage());
					}					
				}
				rowsDocumentos.add(doc);
			}
		}


		//Obtener documento de Anulacion
		if(notificacion.documentoAnulacion != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.ANULACION);
			if (!uriDocAux.equals("")){
				Documento doc = Documento.findByUri(uriDocAux);
				if (doc == null) {
					doc = new Documento();
					doc.uri = uriDocAux;
					doc.firmado = notificacionService.obtenerFirmadoDocumentoNotificacion("", notificacion.uri, DocumentoNotificacionEnumType.ANULACION);					
					try {	
						doc.descripcion = NotificacionUtils.obtenerDescripcionDocumento(uriDocAux);
						doc.tipo = NotificacionUtils.obtenerTipoDocumento(uriDocAux);
					} catch (Exception ex) {
						play.Logger.error("Excepción obteniendo la descripción y el tipo de documento de la uri: " + uriDocAux + "Error: " + ex.getMessage());
					}					
				}
				rowsDocumentos.add(doc);
			}
		}

		
		//Obtener documento de Respuesta
		if(notificacion.documentoRespondida != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.MARCADA_RESPONDIDA);
			if (!uriDocAux.equals("")){
				Documento doc = Documento.findByUri(uriDocAux);
				if (doc == null) {
					doc = new Documento();
					doc.uri = uriDocAux;
					doc.firmado = notificacionService.obtenerFirmadoDocumentoNotificacion("", notificacion.uri, DocumentoNotificacionEnumType.MARCADA_RESPONDIDA);					
					try {	
						doc.descripcion = NotificacionUtils.obtenerDescripcionDocumento(uriDocAux);
						doc.tipo = NotificacionUtils.obtenerTipoDocumento(uriDocAux);
					} catch (Exception ex) {
						play.Logger.error("Excepción obteniendo la descripción y el tipo de documento de la uri: " + uriDocAux + "Error: " + ex.getMessage());
					}					
				}
				rowsDocumentos.add(doc);
			}
		}
		
		//Comprobar documento de noPresentacion
		uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.NO_ACCESO);
		if (!uriDocAux.equals("")){
			Documento doc = Documento.findByUri(uriDocAux);
			if (doc == null) {
				doc = new Documento();
				doc.uri = uriDocAux;
				doc.firmado = notificacionService.obtenerFirmadoDocumentoNotificacion("", notificacion.uri, DocumentoNotificacionEnumType.NO_ACCESO);					
				try {	
					doc.descripcion = NotificacionUtils.obtenerDescripcionDocumento(uriDocAux);
					doc.tipo = NotificacionUtils.obtenerTipoDocumento(uriDocAux);
				} catch (Exception ex) {
					play.Logger.error("Excepción obteniendo la descripción y el tipo de documento de la uri: " + uriDocAux + "Error: " + ex.getMessage());
				}					
			}
			rowsDocumentos.add(doc);
		}
	
		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsDocumentos, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("descripcion", "enlaceDescargaFirmado", "id"));
	}

}
