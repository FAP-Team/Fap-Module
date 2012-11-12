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

public class PresentacionFapController extends InvokeClassController{
	
	public static TramiteBase getTramiteObject (Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		return new TramiteSolicitudFap(solicitud);
	}
	
	public static void beforeRegistro(Long idSolicitud){
	}
	
	public static void afterRegistro(Long idSolicitud){
	}
	
	public static void beforeFirma(Long idSolicitud){
	}
	
	public static void afterFirma(Long idSolicitud){
	}
	
	public static boolean comprobarPaginasGuardadas(Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		ModelUtils.invokeMethodClass(SolicitudGenerica.class, solicitud, "savePagesPrepared");
        if (Messages.hasErrors())
        	return false;
        return true;
	}
	
}
