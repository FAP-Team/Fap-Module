package utils;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import services.NotificacionService;

import models.DocumentoNotificacion;
import models.Interesado;
import models.Notificacion;
import enumerado.fap.gen.EstadoNotificacionEnum;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.EstadoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.EstadoNotificacionType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.InteresadoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionType;

public class NotificacionUtils {
	
	@Inject
    protected static NotificacionService notificacionService;

	public static InteresadoType convertInteresadoToInteresadoType(Interesado interesado){
		InteresadoType interesadoType = new InteresadoType();
		interesadoType.setEmail(interesado.email);
		interesadoType.setMovil(interesado.movil);
		interesadoType.setNif(interesado.persona.getNumeroId());
		interesadoType.setUriTerceros(interesado.uriTerceros);
		if (interesado.persona.tipo.equals("fisica")){
			interesadoType.setApellido1(interesado.persona.fisica.primerApellido);
			interesadoType.setApellido2(interesado.persona.fisica.segundoApellido);
			interesadoType.setNombre(interesado.persona.fisica.nombre);	
			return interesadoType;
		} else if (interesado.persona.tipo.equals("juridica")){
			interesadoType.setApellido1("");
			interesadoType.setApellido2("");
			interesadoType.setNombre(interesado.persona.juridica.entidad);
			return interesadoType;
		} else {// Algo raro
			return null;
		}
	}
	
	public static Notificacion convertNotificacionTypeToNotificacion (NotificacionType notificacionType){
		Notificacion notificacion = new Notificacion();
		notificacion.asunto = notificacionType.getAsunto();
		notificacion.descripcion = notificacionType.getCuerpo();
		notificacion.estado = convertEstadoNotificacionEnumTypeToEstadoNotificacion(notificacionType.getEstadoNotificacion().getEstado());
		notificacion.fechaPuestaADisposicion = new DateTime(notificacionType.getEstadoNotificacion().getFechaCreacion());
		notificacion.idExpedienteAed = notificacionType.getNumeroExpediente();
		notificacion.plazoAcceso = notificacionType.getPlazoAcceso();
		notificacion.plazoRespuesta = notificacionType.getPlazoRespuesta();
		notificacion.frecuenciaRecordatorioAcceso = notificacionType.getRecordatorioAcceso();
		notificacion.frecuenciaRecordatorioRespuesta = notificacionType.getRecordatorioRespuesta();
		notificacion.uri = notificacionType.getUriNotificacion();
		notificacion.uriProcedimiento = notificacionType.getUriProcedimiento();
		for (String uri: notificacionType.getUrisDocumentosAdjuntos())
			notificacion.documentosAnexos.add(new DocumentoNotificacion(uri));
		for (String uri: notificacionType.getUrisDocumentosANotificar())
			notificacion.documentosANotificar.add(new DocumentoNotificacion(uri));
		return notificacion;
	}
	
	public static String convertEstadoNotificacionEnumTypeToEstadoNotificacion (EstadoNotificacionEnumType estadoNotificacionEnumType){
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.ANULADA))
//			return EstadoNotificacionEnum.anulada.name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.CREADA))
//			return EstadoNotificacionEnum..name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.ENVIADA))
//			return EstadoNotificacionEnum..name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.EXPIRADA))
//			return EstadoNotificacionEnum.plazovencido.name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.FINALIZADA))
//			return EstadoNotificacionEnum.cerrada.name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.LEIDA))
//			return EstadoNotificacionEnum.accedida.name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.LEIDA_PLAZO_RESPUESTA_VENCIDO))
//			return EstadoNotificacionEnum..name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.PLAZO_RESPUESTA_VENCIDO))
//			return EstadoNotificacionEnum..name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.PUESTA_A_DISPOSICION))
//			return EstadoNotificacionEnum.puestaadisposicion.name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.RECHAZADA))
//			return EstadoNotificacionEnum..name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.RESPONDIDA))
//			return EstadoNotificacionEnum.respondida.name();
//		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.TERCERO_NO_SUSCRITO))
//			return EstadoNotificacionEnum..name();
		return "";
	}
	
	public static void recargarNotificacionesFromWS (String uriProcedimiento){
		List<Notificacion> notificaciones = notificacionService.getNotificaciones(uriProcedimiento);
		for (Notificacion notificacion: notificaciones){
			Notificacion dbNotificacion = (Notificacion) Notificacion.find("select notificacion from Notificacion notificacion where notificacion.uri=?", notificacion.uri).first();
			if (dbNotificacion != null){
				dbNotificacion.actualizar(notificacion);
			} else {
				dbNotificacion = notificacion;
			}
			dbNotificacion.save();
		}
	}
}
