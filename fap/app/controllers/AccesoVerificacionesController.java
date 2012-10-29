package controllers;

import org.h2.constant.SysProperties;
import org.joda.time.DateTime;

import messages.Messages;
import models.SolicitudGenerica;
import models.Verificacion;
import controllers.gen.AccesoVerificacionesControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class AccesoVerificacionesController extends AccesoVerificacionesControllerGen {
	
	public static void verificacionNueva(Long idSolicitud, String botonIniciarVerificacion) {
		checkAuthenticity();
		if (!permisoVerificacionNueva("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);

		if (!Messages.hasErrors()) {
			AccesoVerificacionesController.verificacionNuevaValidateRules();
		}
		
		if ((!Messages.hasErrors()) /*&& !dbSolicitud.estado.equals(EstadosSolicitudEnum.verificado.name())*/) {
			// Asignamos una nueva verificacion con su fecha de creacion, cuando se pulse el boton de nueva verificación
			dbSolicitud.verificacion = new Verificacion();
			dbSolicitud.verificacion.expediente = dbSolicitud.expedienteAed.idAed;
			dbSolicitud.verificacion.estado = EstadosVerificacionEnum.iniciada.name();
			dbSolicitud.estado = EstadosSolicitudEnum.enVerificacion.name();
			dbSolicitud.verificacion.fechaCreacion = new DateTime();
			dbSolicitud.save();
			long idVerificacion=dbSolicitud.verificacion.id;
			String accion = getAccion();
			redirect("PaginaVerificacionController.index", accion, idSolicitud, idVerificacion);
		
		}
		
		log.info("Acción Editar de página: " + "gen/AccesoVerificaciones/AccesoVerificaciones.html" + " , intentada sin éxito (Problemas de Validación)");
		AccesoVerificacionesController.verificacionNuevaRender(idSolicitud);
	}

}
