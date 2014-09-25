package utils;

import java.util.List;

import javax.xml.datatype.DatatypeConstants;


import messages.Messages;
import messages.Messages.MessageType;
import models.Participacion;
import models.RepresentantePersonaFisica;
import models.RepresentantePersonaJuridica;
import models.Solicitante;
import models.SolicitudGenerica;

import org.joda.time.DateTime;

import play.mvc.Util;

import config.InjectorConfig;
import enumerado.fap.gen.TiposParticipacionEnum;

import services.TercerosService;
import services.TercerosServiceException;

public class ParticipacionRepresentantesUtils {

	public static void BorrarParticipacion (SolicitudGenerica dbSolicitud, String idRepresentante){
		List<Participacion> participaciones = Participacion.findAll();
		for (Participacion participacion: participaciones){
			if ((participacion.agente.username.toUpperCase().equals(idRepresentante)) &&
				(participacion.solicitud.equals(getSolicitud(dbSolicitud.id))) &&
				((participacion.tipo.equals(TiposParticipacionEnum.representante.name())) ||(participacion.tipo.equals(TiposParticipacionEnum.autorizado.name())))
			   ){
				participacion.agente=null;
				participacion.solicitud=null;
				participacion.delete();
				break;
			}
		}
	}
	
	public static void BorrarRepresentantePersonaFisica (SolicitudGenerica dbSolicitud){
		BorrarParticipacionRepresentantePersonaFisica(dbSolicitud);
		dbSolicitud.solicitante.representante.fisica.nip = null;
		dbSolicitud.solicitante.representante.fisica.nombre = null;
		dbSolicitud.solicitante.representante.fisica.primerApellido = null;
		dbSolicitud.solicitante.representante.fisica.segundoApellido = null;
		dbSolicitud.solicitante.representante.email = null;	
	}
	
	public static void BorrarRepresentantePersonaJuridica (SolicitudGenerica dbSolicitud, RepresentantePersonaJuridica representante){
		BorrarParticipacionRepresentantePersonaJuridica(dbSolicitud, representante);
		dbSolicitud.solicitante.representantes.remove(representante);
		dbSolicitud.save();
		RepresentantePersonaFisica representantePF = RepresentantePersonaFisica.findById(representante.id);
		representantePF.delete();
	}
	
	public static void BorrarListaRepresentantesPersonaJuridica (SolicitudGenerica dbSolicitud){
		java.util.List<RepresentantePersonaJuridica> listaRepresentantes = RepresentantePersonaJuridica.find("select representantePersonaJuridica from Solicitud solicitud join solicitud.solicitante.representantes representantePersonaJuridica where solicitud.id=?", dbSolicitud.id).fetch();
		if ((listaRepresentantes!= null) && (listaRepresentantes.size() > 0)){
			for (RepresentantePersonaJuridica representante:listaRepresentantes){
				BorrarRepresentantePersonaJuridica(dbSolicitud, representante);
			}
		}
	}
	
	public static void BorrarParticipacionRepresentantePersonaFisica(SolicitudGenerica dbSolicitud){
		if (dbSolicitud.solicitante.representante.email != null){
			BorrarParticipacion(dbSolicitud, dbSolicitud.solicitante.representante.fisica.nip.valor.toUpperCase());
		}
	}
	
	public static void BorrarParticipacionRepresentantePersonaJuridica (SolicitudGenerica dbSolicitud, RepresentantePersonaJuridica representante){
		if (representante.fisica.nip.valor != null){
			BorrarParticipacion(dbSolicitud, representante.fisica.nip.valor.toUpperCase());
		}
		if (representante.juridica.cif != null){
			BorrarParticipacion(dbSolicitud, representante.juridica.cif.toUpperCase());
		}
	}
	
	public static void BorrarParticipacionesRepresentantesPersonaJuridica (SolicitudGenerica dbSolicitud){
		java.util.List<RepresentantePersonaJuridica> listaRepresentantes = RepresentantePersonaJuridica.find("select representantePersonaJuridica from Solicitud solicitud join solicitud.solicitante.representantes representantePersonaJuridica where solicitud.id=?", dbSolicitud.id).fetch();
		if (listaRepresentantes != null){
			for (RepresentantePersonaJuridica representante:listaRepresentantes){
				if (representante.fisica.nip.valor != null){
					BorrarParticipacion(dbSolicitud, representante.fisica.nip.valor.toUpperCase());
				}
				if (representante.juridica.cif != null){
					BorrarParticipacion(dbSolicitud, representante.juridica.cif.toUpperCase());
				}
			}
		}
	}
	
	@Util
	public static SolicitudGenerica getSolicitud(Long idSolicitud) {
		SolicitudGenerica solicitud = null;
		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		} else {
			solicitud = SolicitudGenerica.findById(idSolicitud);
			if (solicitud == null) {
				Messages.fatal("Error al recuperar Solicitud");
			}
		}
		return solicitud;
	}
	
	@Util
	public static void BorrarParticipaciones(SolicitudGenerica dbSolicitud) {
		BorrarParticipacionRepresentantePersonaFisica(dbSolicitud);
		BorrarParticipacionesRepresentantesPersonaJuridica(dbSolicitud);
	}
	
}