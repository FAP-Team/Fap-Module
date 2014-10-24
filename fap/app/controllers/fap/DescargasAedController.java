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

import models.Documento;
import org.joda.time.DateTime;

import config.InjectorConfig;

import play.Logger;
import play.libs.Codec;
import play.libs.IO;
import play.mvc.Router;
import play.mvc.Http.Response;
import play.mvc.Router.ActionDefinition;
import play.mvc.Util;

import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.platino.PlatinoGestorDocumentalService;
import utils.AedUtils;
import utils.BinaryResponse;

import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.platino.servicios.sgrde.ElementoNoEncontradoException;
import es.gobcan.platino.servicios.sgrde.ErrorInternoException;
import es.gobcan.platino.servicios.sgrde.IdDocumentoItem;
import es.gobcan.platino.servicios.sgrde.UsuarioNoValidoException;
import es.gobcan.platino.servicios.tramitacion.DocumentoItem;
import messages.Messages;
import models.*;


public class DescargasAedController extends GenericController {
	
	@Inject
	static GestorDocumentalService aedService;
	
	@Inject
	static PlatinoGestorDocumentalService platinoaed;
	
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
	 * Descarga un documento Firmado del archivo electrónico.
	 * 
	 * Si el documento existe en el Aed de Platino se descarga el documento con
	 * la firma de platino. 
	 * En caso contrario, pero que exista en el Aed de la ACIISI
	 * se descarga el documento con la firma de ACIISI.
	 * 
	 * @param k Hash calculado a partir de la uri y de la fecha actual
	 * @param uri
	 */
	public static void descargarFirmado(String k){
		String uri= AedUtils.desencriptarUri(k);
        Documento documento = buscarDocumentoAed(uri);
        es.gobcan.platino.servicios.sgrde.Documento documentoPlatino = buscarDocumentoPlatinoAed(uri,documento);
        if (documentoPlatino.getContenido() != null){
            generarPdfResponse(documentoPlatino.getContenido(),"");
		} else if(uri != null){
            BinaryResponse bresp = buscarDocumentoFirmadoAed(uri, documento);
            generarPdfResponse(bresp.contenido, bresp.nombre);
        }else {
            forbidden("No tiene permisos para acceder a este documento");
		}
	}

    private static Documento buscarDocumentoAed(String uri) {
        Documento documento = new Documento();
        if(uri != null) {
            documento = Documento.findByUri(uri);
        }
        return documento;
    }

    private static es.gobcan.platino.servicios.sgrde.Documento buscarDocumentoPlatinoAed(String uri, Documento documento) {
        es.gobcan.platino.servicios.sgrde.Documento documentoPlatino = new es.gobcan.platino.servicios.sgrde.Documento();
        if((documento != null) && (!documento.anexo) && (documento.anexo != null) && (documento.uriPlatino != null)) {
            documentoPlatino = platinoaed.descargarFirmado(uri);
        }
        return documentoPlatino;
    }

    private static void generarPdfResponse(DataHandler dataHandler, String fileName) {
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        response.contentType =dataHandler.getContentType();

        try {
            IO.write(dataHandler.getInputStream(), response.out);
        } catch (IOException e) {
            play.Logger.error("Se produjo un error generando el documento: " + e);
        }
    }

    private static BinaryResponse buscarDocumentoFirmadoAed(String uri, Documento documento){
        BinaryResponse bresp = null;
        try {
            if (documento == null) {
                bresp = aedService.getDocumentoConInformeDeFirmaByUri(uri);
                if (bresp == null) {
                    bresp = aedService.getDocumentoByUri(uri);
                    if (bresp == null)
                        notFound();
                }
            } else {
                bresp = aedService.getDocumentoConInformeDeFirma(documento);
                if (bresp == null) {
                    bresp = aedService.getDocumento(documento);
                    if (bresp == null)
                        notFound();
                }
            }
        } catch (GestorDocumentalServiceException e) {
            Logger.info("Error al obtener documento firmado AED en DescargasAedController: " + e.getMessage());
        }
        return bresp;
    }


	/**
	 * Controlador intermedio necesario porque de una plantilla html no podemos invocar directamente 
	 * un método de la interfaz GestorDocumentalService.
	 * 
	 * Llama al método de la interfaz getDocumentosPorTipo (obtenemos los documentos, de tipo tipoDocumento, 
	 * de las solicitudes donde el agente actualmente logueado es solicitante).
	 * 
	 * @param tipoDocumento Tipo del documento 
	 * @throws GestorDocumentalServiceException 
	 * 
	 */
	public static void getDocumentosPorTipoGestorDocumental(String tipoDocumento) throws GestorDocumentalServiceException {
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		List<Documento> rows = gestorDocumentalService.getDocumentosPorTipo(tipoDocumento);
		
		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rows);
		renderJSON(response.toJSON("uri", "descripcion", "urlDescarga"));	
	}
	
}
