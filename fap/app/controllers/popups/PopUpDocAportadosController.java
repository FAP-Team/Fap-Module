
package controllers.popups;

import play.mvc.Util;
import messages.Messages;
import models.Documento;
import controllers.gen.popups.PopUpDocAportadosControllerGen;
			
public class PopUpDocAportadosController extends PopUpDocAportadosControllerGen {

    protected static Documento getDocumento(Long idSolicitud, Long idDocumento){
    	System.out.println("hola soy hijo");
        Documento documento = null;
        if(idSolicitud == null){
            Messages.fatal("Falta parámetro idSolicitud");
        }else if(idDocumento == null){
            Messages.fatal("Falta parámetro idDocumento");
        }else{
			documento = Documento.find("select registradas.justificante from Solicitud solicitud join solicitud.aportaciones.registradas registradas where solicitud.id=? and solicitud.aportaciones.actual.id=?",
					idSolicitud, idDocumento).first();
            if(documento == null){
                Messages.fatal("Error al recuperar Documento");
            }
        }
        return documento;
    }
    
    @Util
	public static void abrir(String accion,Long idDocumento,Long idSolicitud){
		Documento documento;
		if(accion.equals("crear")){
            documento = new Documento();
		}else{
		    documento = getDocumento(idSolicitud, idDocumento);
		}
		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		renderTemplate("gen/popups/PopUpDocAportados.html",accion,idDocumento,documento,idSolicitud);
	}
}
		