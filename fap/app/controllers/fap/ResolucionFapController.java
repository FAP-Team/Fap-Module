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

public class ResolucionFapController extends InvokeClassController{
	
	public static void setEstadoAfterResolucion(Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		if (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("acepta")) {
			solicitud.estado = EstadosSolicitudEnum.aceptadoRSLPROV.name();
		} else {
			solicitud.estado = EstadosSolicitudEnum.renunciadoRSLPROV.name();
		}
		solicitud.save();
	}

}
