package controllers.fap;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.inject.Inject;

import org.joda.time.DateTime;

import config.InjectorConfig;

import play.libs.Codec;
import play.libs.IO;
import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import play.mvc.Util;

import services.GestorDocumentalService;
import utils.AedUtils;
import utils.BinaryResponse;

import es.gobcan.eadmon.aed.ws.AedExcepcion;
import aed.AedClient;
import messages.Messages;
import models.*;


public class DescargasAedController extends GenericController {
	
	@Inject
	static GestorDocumentalService aedService;
	
	/**
	 * Descarga un documento del archivo electrónico
	 * @param k Hash calculado a partir de la uri y de la fecha actual
	 * @param uri
	 */
	public static void descargar(String k){
		String uri= AedUtils.desencriptarUri(k);
		
		if(uri != null){
			BinaryResponse bresp;
			try {
			    Documento documento = Documento.findByUri(uri);
				if(documento == null) {
					bresp = aedService.getDocumentoByUri(uri);
					if(bresp == null) 
						notFound();
				}
				else {
					bresp = aedService.getDocumento(documento);
				}
				
	            response.setHeader("Content-Disposition", "inline; filename=\"" + bresp.nombre + "\"");
	            response.contentType = bresp.contenido.getContentType();
	            
	            // FIX IE bug when using SSL
//	            if(request.secure && isIE(request))
//	            	response.setHeader("Cache-Control", "");
				
	            IO.write(bresp.contenido.getInputStream(), response.out);
			} catch (Exception e) {
				play.Logger.error(e, "Se produjo un error recuperando el documento del AED");
			}
		}else{
			forbidden("No tiene permisos para acceder a este documento");
		}
	}
	
	/**
	 * Controlador intermedio necesario porque de una plantilla html no podemos invocar directamente 
	 * un método de la interfaz GestorDocumentalService.
	 * 
	 * Llama al método de la interfaz getDocumentosPorTipo (obtenemos los documentos, de tipo tipoDocumento, 
	 * de las solicitudes donde el agente actualmente logueado es solicitante).
	 * 
	 * @param tipoDocumento Tipo del documento 
	 * 
	 */
	public static void getDocumentosPorTipoGestorDocumental(String tipoDocumento) throws AedExcepcion {
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		List<Documento> rows = gestorDocumentalService.getDocumentosPorTipo(tipoDocumento);
		
		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rows);
		renderJSON(response.toJSON("uri", "descripcion", "urlDescarga"));	
	}
	
	/**
	 * Función a la que se llama cuando hemos seleccionado un archivo ya subido anteriormente para volver a usarlo en otra solicitud.
	 * 
	 * Llama al método de la interfaz GestorDocumentalService duplicarDocumentoSubido.
	 * 
	 */
	public static void asignarASolicitudDocumentoSubido(String uriDocumento) {
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Long idSolicitud = ids.get("idSolicitud");
		if (idSolicitud == null) {
			Messages.fatal("Falta parámetro idSolicitud");
			return;
		}
		SolicitudGenerica solicitud = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.id = " + idSolicitud.toString()).first();

		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		try {
			gestorDocumentalService.duplicarDocumentoSubido(uriDocumento, solicitud);
		} catch (Exception e) { 
			//System.out.println(e);
			Messages.error("Ha habido un error al subir el documento"); 
			Messages.keep();
		    redirect("SubirArchivoAedTestController.index", "editar", idSolicitud);
		}
		Messages.ok("Documento subido correctamente");
		Messages.keep();
		redirect("SubirArchivoAedTestController.index", "editar", idSolicitud);
	}
	
}
