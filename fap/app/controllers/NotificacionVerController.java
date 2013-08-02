package controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Documento;
import models.DocumentoNotificacion;
import models.Notificacion;
import play.mvc.Util;
import properties.FapProperties;
import utils.AedUtils;
import utils.NotificacionUtils;
import controllers.gen.NotificacionVerControllerGen;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;

public class NotificacionVerController extends NotificacionVerControllerGen {
	
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
		java.util.List<DocumentoNotificacion> rows = DocumentoNotificacion.find("select documentoNotificacion from Notificacion notificacion join notificacion.documentosANotificar documentoNotificacion where notificacion.id=?", idNotificacion).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		Notificacion notificacion = Notificacion.findById(idNotificacion);
		List<Documento> rowsDocumentos = new ArrayList<Documento>();
		String uriDocAux = "";
		
		//Obtener documento de puestaADisposicion
		if(notificacion.documentoPuestaADisposicion != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.PUESTA_A_DISPOSICION);
			if (!uriDocAux.equals("")){
				Documento doc = new Documento();
				doc.uri = uriDocAux;
				doc.enlaceDescargaFirmado = "<a href=\""+AedUtils.crearUrlConInformeDeFirma(uriDocAux)+"\" target=\"_blank\">Descargar Firmado</a>";
				doc.descripcion = DocumentoNotificacionEnumType.PUESTA_A_DISPOSICION.value();
				rowsDocumentos.add(doc);
			}
		}
		//Obtener documento de acuseDeRecibo
		if(notificacion.documentoAcuseRecibo != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.ACUSE_RECIBO);
			if (!uriDocAux.equals("")){
				Documento doc = new Documento();
				doc.uri = uriDocAux;
				doc.enlaceDescargaFirmado = "<a href=\""+AedUtils.crearUrlConInformeDeFirma(uriDocAux)+"\" target=\"_blank\">Descargar Firmado</a>";
				doc.descripcion = DocumentoNotificacionEnumType.ACUSE_RECIBO.value();
				rowsDocumentos.add(doc);
			}
		}


		//Obtener documento de Anulacion
		if(notificacion.documentoAnulacion != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.ANULACION);
			if (!uriDocAux.equals("")){
				Documento doc = new Documento();
				doc.uri = uriDocAux;
				doc.enlaceDescargaFirmado = "<a href=\""+AedUtils.crearUrlConInformeDeFirma(uriDocAux)+"\" target=\"_blank\">Descargar Firmado</a>";
				doc.descripcion = DocumentoNotificacionEnumType.ANULACION.value();
				rowsDocumentos.add(doc);
			}
		}

		
		//Obtener documento de Respuesta
		if(notificacion.documentoRespondida != null){
			uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.MARCADA_RESPONDIDA);
			if (!uriDocAux.equals("")){
				Documento doc = new Documento();
				doc.uri = uriDocAux;
				doc.enlaceDescargaFirmado = "<a href=\""+AedUtils.crearUrlConInformeDeFirma(uriDocAux)+"\" target=\"_blank\">Descargar Firmado</a>";
				doc.descripcion = DocumentoNotificacionEnumType.MARCADA_RESPONDIDA.value();
				rowsDocumentos.add(doc);
			}
		}
		
		//Comprobar documento de noPresentacion
		uriDocAux = NotificacionUtils.obtenerUriDocumentos(notificacion, DocumentoNotificacionEnumType.NO_ACCESO);
		if (!uriDocAux.equals("")){
			Documento doc = new Documento();
			doc.uri = uriDocAux;
			doc.enlaceDescargaFirmado = "<a href=\""+AedUtils.crearUrlConInformeDeFirma(uriDocAux)+"\" target=\"_blank\">Descargar Firmado</a>"; 
			doc.descripcion = DocumentoNotificacionEnumType.NO_ACCESO.value();
			rowsDocumentos.add(doc);
		}
	
		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsDocumentos, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("descripcion", "enlaceDescargaFirmado", "id"));
	}

}
