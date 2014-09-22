package controllers.popups;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Agente;
import models.AutorizacionesFAP;
import models.Nip;
import models.Participacion;
import models.SolicitudGenerica;
import play.mvc.Util;
import validation.CifCheck;
import validation.CustomValidation;
import validation.NipCheck;
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
		busqueda(idSolicitud,  autorizacionesFAP.numeroIdentificacion, dbAutorizacionesFAP);
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
		busqueda(idSolicitud,  autorizacionesFAP.numeroIdentificacion, dbAutorizacionesFAP);
		if (!Messages.hasErrors()) {
			PopUpAutorizarFAPController.PopUpAutorizarFAPValidateCopy("crear", dbAutorizacionesFAP, autorizacionesFAP);
		}
		
        if (!Messages.hasErrors()) {
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
		if (secure.checkGrafico("autorizadoNoAutoriza", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("autorizacionesFAP", autorizacionesFAP);
			CustomValidation.required("autorizacionesFAP.numeroIdentificacion", autorizacionesFAP.numeroIdentificacion);
			NipCheck nipCheck = new NipCheck();
			Nip nipAux = new Nip();
			nipAux.tipo="nif";
			nipAux.valor=autorizacionesFAP.numeroIdentificacion;
			StringBuilder texto = new StringBuilder();
			if (!nipCheck.validaNip(nipAux, texto)){
				nipAux.tipo="nie";
				nipAux.valor=autorizacionesFAP.numeroIdentificacion;
				if (!nipCheck.validaNip(nipAux, texto)){
					CifCheck cifCheck = new CifCheck();
					if (!cifCheck.validaCif(autorizacionesFAP.numeroIdentificacion, texto))
						CustomValidation.error("El NIF/CIF tiene un formato incorrecto", "autorizacionesFAP.numeroIdentificacion", autorizacionesFAP.numeroIdentificacion);
				}
			}
			if (!Messages.hasErrors()){
				dbAutorizacionesFAP.numeroIdentificacion = autorizacionesFAP.numeroIdentificacion;
                dbAutorizacionesFAP.nombreAutorizado = autorizacionesFAP.nombreAutorizado;
			}
		}
	}
	
	@Util
	public static void busqueda(Long idSolicitud, String identificador, AutorizacionesFAP autorizacionesFAP) {
		//Tengo que buscar todas las autorizaciones asociadas a la solicitud
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		List<AutorizacionesFAP> listaAuto = solicitud.autorizacion;
		for (AutorizacionesFAP auto : listaAuto) {
			if (auto.numeroIdentificacion.toUpperCase().equals(identificador.toUpperCase())){
				CustomValidation.error("Ese NIF/CIF ya ha sido autorizado para esta solicitud", "autorizacionesFAP.numeroIdentificacion", autorizacionesFAP.numeroIdentificacion);
				break;
			}
		}
	}
	
	@Util
	public static void AsignarAgente(Long idSolicitud, AutorizacionesFAP autorizaciones) {
		boolean encontrado = false;
		List<Agente> listaAgentes = Agente.findAll();
		for (Agente ag : listaAgentes) {
			if (ag.username.toUpperCase().equals(autorizaciones.numeroIdentificacion.toUpperCase())){
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
			ag.username = autorizaciones.numeroIdentificacion.toUpperCase();
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
			if ((participacion.agente.username.toUpperCase().equals(autorizacion.numeroIdentificacion.toUpperCase())) &&
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
