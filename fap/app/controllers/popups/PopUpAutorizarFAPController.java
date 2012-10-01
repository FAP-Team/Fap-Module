package controllers.popups;

import java.util.List;

import messages.Messages;
import models.Agente;
import models.AutorizacionesFAP;
import models.Nip;
import models.Participacion;
import models.SolicitudGenerica;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.PopUpAutorizarFAPControllerGen;
import enumerado.fap.gen.TiposParticipacionEnum;

public class PopUpAutorizarFAPController extends PopUpAutorizarFAPControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitud, Long idAutorizacionesFAP, AutorizacionesFAP autorizacionesFAP) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		AutorizacionesFAP dbAutorizacionesFAP = PopUpAutorizarFAPController.getAutorizacionesFAP(idSolicitud, idAutorizacionesFAP);

		PopUpAutorizarFAPController.PopUpAutorizarFAPBindReferences(autorizacionesFAP);
		busqueda(idSolicitud,  autorizacionesFAP.nip, dbAutorizacionesFAP);
		if (!Messages.hasErrors()) {
			PopUpAutorizarFAPController.PopUpAutorizarFAPValidateCopy("editar", dbAutorizacionesFAP, autorizacionesFAP);
			AsignarAgente(idSolicitud, autorizacionesFAP);
		}

		if (!Messages.hasErrors()) {
			PopUpAutorizarFAPController.editarValidateRules(dbAutorizacionesFAP, autorizacionesFAP);
		}
		if (!Messages.hasErrors()) {
			dbAutorizacionesFAP.save();
			log.info("Acción Editar de página: " + "gen/popups/PopUpAutorizarFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/popups/PopUpAutorizarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		PopUpAutorizarFAPController.editarRender(idSolicitud, idAutorizacionesFAP);
	}
	
	@Util
	public static Long crearLogica(Long idSolicitud, AutorizacionesFAP autorizacionesFAP) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		AutorizacionesFAP dbAutorizacionesFAP = PopUpAutorizarFAPController.getAutorizacionesFAP();
		SolicitudGenerica dbSolicitud = PopUpAutorizarFAPController.getSolicitudGenerica(idSolicitud);

		PopUpAutorizarFAPController.PopUpAutorizarFAPBindReferences(autorizacionesFAP);
		busqueda(idSolicitud,  autorizacionesFAP.nip, dbAutorizacionesFAP);
		if (!Messages.hasErrors()) {
			PopUpAutorizarFAPController.PopUpAutorizarFAPValidateCopy("crear", dbAutorizacionesFAP, autorizacionesFAP);
			AsignarAgente(idSolicitud, autorizacionesFAP);
		}

		if (!Messages.hasErrors()) {
			PopUpAutorizarFAPController.crearValidateRules(dbAutorizacionesFAP, autorizacionesFAP);
		}
		Long idAutorizacionesFAP = null;
		if (!Messages.hasErrors()) {

			dbAutorizacionesFAP.save();
			idAutorizacionesFAP = dbAutorizacionesFAP.id;
			dbSolicitud.autorizacion.add(dbAutorizacionesFAP);
			dbSolicitud.save();

			log.info("Acción Crear de página: " + "gen/popups/PopUpAutorizarFAP.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/popups/PopUpAutorizarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idAutorizacionesFAP;
	}
	
	@Util
	public static void PopUpAutorizarFAPValidateCopy(String accion, AutorizacionesFAP dbAutorizacionesFAP, AutorizacionesFAP autorizacionesFAP) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("autorizacionesFAP.nip", autorizacionesFAP.nip);
		CustomValidation.valid("autorizacionesFAP", autorizacionesFAP);
		CustomValidation.required("autorizacionesFAP.nip", autorizacionesFAP.nip);
		dbAutorizacionesFAP.nip.tipo = autorizacionesFAP.nip.tipo;
		dbAutorizacionesFAP.nip.valor = autorizacionesFAP.nip.valor.toUpperCase();

	}
	
	@Util
	public static void busqueda(Long idSolicitud, Nip nip, AutorizacionesFAP autorizacionesFAP) {
		//Tengo que buscar todas las autorizaciones asociadas a la solicitud
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		List<AutorizacionesFAP> listaAuto = solicitud.autorizacion;
		for (AutorizacionesFAP auto : listaAuto) {
			if (auto.nip.valor.toUpperCase().equals(nip.valor.toUpperCase())){
				CustomValidation.error("Ese nip ya ha sido autorizado para esta solicitud", "autorizacionesFAP.nip.valor", autorizacionesFAP.nip.valor);
				break;
			}
		}
	}
	
	@Util
	public static void AsignarAgente(Long idSolicitud, AutorizacionesFAP autorizaciones) {
		boolean encontrado = false;
		List<Agente> listaAgentes = Agente.findAll();
		for (Agente ag : listaAgentes) {
			if (ag.username.toUpperCase().equals(autorizaciones.nip.valor.toUpperCase())){
				encontrado = true;
				Participacion p = new Participacion();
				p.agente = ag;
				p.solicitud = getSolicitudGenerica(idSolicitud);
				p.tipo = TiposParticipacionEnum.autorizado.name();
				p.save();
				break;
			}
		}
		if (!encontrado){
			Agente ag = new Agente();
			ag.username = autorizaciones.nip.valor.toUpperCase();
			ag.roles.add("usuario");
			ag.rolActivo = "usuario";
			ag.save();
			
			Participacion p = new Participacion();
			p.agente = ag;
			p.solicitud = getSolicitudGenerica(idSolicitud);
			p.tipo = TiposParticipacionEnum.autorizado.name();
			p.save();
		}
	}
	
	public static void borrar(Long idSolicitud, Long idAutorizacionesFAP) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		AutorizacionesFAP dbAutorizacionesFAP = PopUpAutorizarFAPController.getAutorizacionesFAP(idSolicitud, idAutorizacionesFAP);
		SolicitudGenerica dbSolicitud = PopUpAutorizarFAPController.getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			PopUpAutorizarFAPController.borrarValidateRules(dbAutorizacionesFAP);
		}
		if (!Messages.hasErrors()) {
			BorrarParticipacion(idSolicitud, dbAutorizacionesFAP);
			dbSolicitud.autorizacion.remove(dbAutorizacionesFAP);
			dbSolicitud.save();
			dbAutorizacionesFAP.delete();

			log.info("Acción Borrar de página: " + "gen/popups/PopUpAutorizarFAP.html" + " , intentada con éxito");
		} else {
			log.info("Acción Borrar de página: " + "gen/popups/PopUpAutorizarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PopUpAutorizarFAPController.borrarRender(idSolicitud, idAutorizacionesFAP);
	}
	
	@Util
	public static void BorrarParticipacion(Long idSolicitud, AutorizacionesFAP autorizacion) {
		List<Participacion> participaciones = Participacion.findAll();
		for (Participacion participacion: participaciones){
			if ((participacion.agente.username.toUpperCase().equals(autorizacion.nip.valor.toUpperCase())) &&
				(participacion.solicitud.equals(getSolicitudGenerica(idSolicitud))) &&
				(participacion.tipo.equals(TiposParticipacionEnum.autorizado.name()))
			   ){
				participacion.agente=null;
				participacion.solicitud=null;
				participacion.delete();
				break;
			}
		}
	}
	
}
