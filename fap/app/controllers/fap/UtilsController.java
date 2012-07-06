package controllers.fap;

import java.util.ArrayList;
import java.util.List;

import enumerado.fap.gen.EstadoNotificacionEnum;

import models.Agente;
import models.Interesado;
import models.Notificacion;
import play.mvc.*;
import utils.DocumentosUtils;

public class UtilsController extends Controller {

    public static boolean documentoEsMultiple(String tipoUri) {
    	return DocumentosUtils.esTipoMultiple(tipoUri);
    }
    
    public static Integer getNuevasNotificaciones(String usuario){
    	List<Notificacion> notificaciones = Notificacion.find("select notificacion from Notificacion notificacion where notificacion.estado=?", EstadoNotificacionEnum.puestaadisposicion.name()).fetch();
    	List<Notificacion> misNotificaciones = new ArrayList<Notificacion>();
    	boolean esMiNotificacion=false;
    	if (notificaciones != null){
	    	for (Notificacion notificacion: notificaciones){
	    		for (Interesado interesado: notificacion.interesados){
	    			if ((interesado.persona.getNumeroId() != null) && (interesado.persona.getNumeroId().equals(usuario))){
	    				esMiNotificacion=true;
	    				break;
	    			}
	    		}
	    		if (esMiNotificacion){
	    			misNotificaciones.add(notificacion);
	    			esMiNotificacion=false;
	    		}
	    	}
    	} else {
    		return 0;
    	}
    	return misNotificaciones.size();
    }

}
