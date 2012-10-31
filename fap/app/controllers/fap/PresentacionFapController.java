package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import enumerado.fap.gen.EstadosSolicitudEnum;

import messages.Messages;
import models.SolicitudGenerica;
import tramitacion.*;
import utils.ModelUtils;

import play.Play;
import play.utils.Java;

public class PresentacionFapController {
	
	public static TramiteBase getTramiteObject (Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		return new TramiteSolicitudFap(solicitud);
	}
	
	public static void afterRegistro(Long idSolicitud){
	}
	
	public static void beforeFirma(Long idSolicitud){
	}
	
	public static boolean comprobarPaginasGuardadas(Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		ModelUtils.invokeMethodClass(SolicitudGenerica.class, solicitud, "savePagesPrepared");
        if (Messages.hasErrors())
        	return false;
        return true;
	}
	
	// TODO: Este metodo es temporal, se deberia crear un Controller propio para la AceptacionRenuncia con su invoke
	public static void setEstadoAfterResolucion(Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		if (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("acepta")) {
			solicitud.estado = EstadosSolicitudEnum.aceptadoRSLPROV.name();
		} else {
			solicitud.estado = EstadosSolicitudEnum.renunciadoRSLPROV.name();
		}
		solicitud.save();
	}
	
	public static <T> T invoke(String m, Object... args) throws Throwable {
		Class claseDelMetodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(PresentacionFapController.class);
        if(classes.size() != 0) {
        	claseDelMetodoALlamar = classes.get(0);
        } else {
        	return (T)Java.invokeStatic(PresentacionFapController.class, m, args);
        }
        try {
        	return (T)Java.invokeStaticOrParent(claseDelMetodoALlamar, m, args);
        } catch(InvocationTargetException e) {
        	throw e.getTargetException();
        }
	}

}
