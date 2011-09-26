package controllers.fap;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.joda.time.DateTime;

import play.libs.Codec;
import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import play.mvc.Util;

import utils.AedUtils;
import utils.BinaryResponse;

import es.gobcan.eadmon.aed.ws.AedExcepcion;
import aed.AedClient;
import models.*;

public class DescargasAedController extends GenericController {
	
	/**
	 * Descarga un documento del archivo electrónico
	 * @param k Hash calculado a partir de la uri y de la fecha actual
	 * @param uri
	 */
	public static void descargar(String k){
		String uri= AedUtils.desencriptarUri(k);
		
		if(uri != null){
			try {
				BinaryResponse bresp = AedClient.obtenerDoc(uri);
				if(bresp == null){
					notFound();
				}
				
	            response.setHeader("Content-Disposition", "inline; filename=\"" + bresp.nombre + "\"");
	            response.contentType = bresp.contenido.getContentType();
	            
	            // FIX IE bug when using SSL
//	            if(request.secure && isIE(request))
//	            	response.setHeader("Cache-Control", "");
				
				InputStream is = bresp.contenido.getInputStream();
                byte[] buffer = new byte[8092];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    response.out.write(buffer, 0, count);
                }
                is.close();
				
				
			} catch (Exception e) {
				play.Logger.error(e, "Se produjo un error recuperando el documento del AED");
			}
		}else{
			forbidden("No tiene permisos para acceder a este documento");
		}
	}

	
}
