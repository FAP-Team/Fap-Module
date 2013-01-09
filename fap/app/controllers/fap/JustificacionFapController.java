package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import enumerado.fap.gen.EstadosSolicitudEnum;

import messages.Messages;
import models.SolicitudGenerica;
import tramitacion.*;
import utils.ModelUtils;

import play.Play;
import play.utils.Java;
import properties.FapProperties; 

public class JustificacionFapController {
		
		public static TramiteBase getTramiteObject (Long idSolicitud){
			SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
			return new TramiteJustificacion(solicitud);
		}
		
		public static void beforeRegistro(Long idSolicitud){
		}
		
		public static void afterRegistro(Long idSolicitud){
		}
		
		public static void beforeFirma(Long idSolicitud){
		}
		
		public static void afterFirma(Long idSolicitud){
		}
		
		public static void comprobarFechaLimiteJustificacion(Long idSolicitud){
			try {
				if (FapProperties.get("fap.app.justificacion.fechacierre") != null){
					String fechaStr = FapProperties.get("fap.app.justificacion.fechacierre");
					DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
					DateTime fechaLimite = formatter.parseDateTime(fechaStr);
					if (fechaLimite.isBeforeNow()){
						play.Logger.error("La justificaacion de la solicitud "+idSolicitud+" no se ha podido presentar (registrar o firmar). La fecha Límite de Justificación ha expirado: "+fechaStr);
						Messages.keep();
					}
				}
			} catch (Exception e){
				play.Logger.error("Fallo recuperando y verificando la fecha de cierre de la justificación de la aplicación");
			}
		}
		
		public static <T> T invoke(String m, Object... args) throws Throwable {
			Class claseDelMetodoALlamar = null;
	        List<Class> classes = Play.classloader.getAssignableClasses(JustificacionFapController.class);
	        if(classes.size() != 0) {
	        	claseDelMetodoALlamar = classes.get(0);
	        } else {
	        	return (T)Java.invokeStatic(JustificacionFapController.class, m, args);
	        }
	        try {
	        	return (T)Java.invokeStaticOrParent(claseDelMetodoALlamar, m, args);
	        } catch(InvocationTargetException e) {
	        	throw e.getTargetException();
	        }
		}

	
}
