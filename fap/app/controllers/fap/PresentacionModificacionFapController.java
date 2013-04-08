package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import enumerado.fap.gen.EstadosSolicitudEnum;

import messages.Messages;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import tramitacion.*;
import utils.ModelUtils;

import play.Play;
import play.utils.Java;
import properties.FapProperties;

public class PresentacionModificacionFapController {
	
	public static TramiteBase getTramiteObject (Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		return new TramiteSolicitudModificacionFap(solicitud);
	}
	
	public static void beforeRegistro(Long idSolicitud){
	}
	
	public static void afterRegistro(Long idSolicitud){
	}
	
	public static void beforeFirma(Long idSolicitud){
	}
	
	public static void afterFirma(Long idSolicitud){
	}
	
	public static <T> T invoke(String m, Object... args) throws Throwable {
		Class claseDelMetodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(PresentacionModificacionFapController.class);
        if(classes.size() != 0) {
        	claseDelMetodoALlamar = classes.get(0);
        } else {
        	return (T)Java.invokeStatic(PresentacionModificacionFapController.class, m, args);
        }
        try {
        	return (T)Java.invokeStaticOrParent(claseDelMetodoALlamar, m, args);
        } catch(InvocationTargetException e) {
        	throw e.getTargetException();
        }
	}
	
	public static void comprobarFechaLimitePresentacion(Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		if (solicitud.registroModificacion.isEmpty()){
			Messages.error("La fecha Límite de Presentación de la modificación no se reconoce correctamente.");
			return;
		}
		RegistroModificacion registroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
		if (registroModificacion.fechaLimite.isBeforeNow()){
			play.Logger.error("La solicitud "+idSolicitud+" no se ha podido presentar (registrar o firmar). La fecha Límite de Presentación de la modificación ha expirado: "+registroModificacion.fechaLimite.toString());
			Messages.error("La fecha Límite de Presentación de la modificación ha expirado: "+registroModificacion.fechaLimite.toString());
			Messages.keep();
		}
	}

}
