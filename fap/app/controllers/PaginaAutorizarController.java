package controllers;

import java.util.List;

import messages.Messages;
import models.Agente;
import models.Nip;
import models.Participacion;
import models.SolicitudGenerica;
import models.Autorizaciones;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.PaginaAutorizarControllerGen;
import enumerado.fap.gen.TiposParticipacionEnum;

public class PaginaAutorizarController extends PaginaAutorizarControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitud, Long idAutorizaciones, Autorizaciones autorizaciones) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		Autorizaciones dbautorizaciones = PaginaAutorizarController.getAutorizaciones(idSolicitud, idAutorizaciones);

		PaginaAutorizarController.PaginaAutorizarBindReferences(autorizaciones);
		busqueda(idSolicitud,  autorizaciones.nip, dbautorizaciones);
		if (!Messages.hasErrors()) {

			PaginaAutorizarController.PaginaAutorizarValidateCopy("editar", dbautorizaciones, autorizaciones);
			AsignarAgente(idSolicitud, autorizaciones);
		}

		if (!Messages.hasErrors()) {
			PaginaAutorizarController.editarValidateRules(dbautorizaciones, autorizaciones);
		}
		if (!Messages.hasErrors()) {
			dbautorizaciones.save();
			
			log.info("Acción Editar de página: " + "gen/PaginaAutorizar/PaginaAutorizar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAutorizar/PaginaAutorizar.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaAutorizarController.editarRender(idSolicitud, idAutorizaciones);
	}
	
	@Util
	public static Long crearLogica(Long idSolicitud, Autorizaciones autorizaciones) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		Autorizaciones dbautorizaciones = PaginaAutorizarController.getAutorizaciones();
		SolicitudGenerica dbSolicitud = PaginaAutorizarController.getSolicitudGenerica(idSolicitud);

		PaginaAutorizarController.PaginaAutorizarBindReferences(autorizaciones);
		busqueda(idSolicitud,  autorizaciones.nip, dbautorizaciones);
		if (!Messages.hasErrors()) {
			PaginaAutorizarController.PaginaAutorizarValidateCopy("crear", dbautorizaciones, autorizaciones);
			AsignarAgente(idSolicitud, autorizaciones);
		}

		if (!Messages.hasErrors()) {
			PaginaAutorizarController.crearValidateRules(dbautorizaciones, autorizaciones);
		}
		
		Long idAutorizaciones = null;
		if (!Messages.hasErrors()) {

			dbautorizaciones.save();
			idAutorizaciones = dbautorizaciones.id;
			dbSolicitud.autorizacion.add(dbautorizaciones);
			dbSolicitud.save();

			log.info("Acción Crear de página: " + "gen/PaginaAutorizar/PaginaAutorizar.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/PaginaAutorizar/PaginaAutorizar.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idAutorizaciones;
	}

	@Util
	public static void busqueda(Long idSolicitud, Nip nip, Autorizaciones autorizaciones) {
		//Tengo que buscar todas las autorizaciones asociadas a la solicitud
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		List<Autorizaciones> listaAuto = solicitud.autorizacion;
		for (Autorizaciones auto : listaAuto) {
			if (auto.nip.valor.equals(nip.valor)){
				Messages.error("Ese nip ya ha sido autorizado para esta solicitud");
				break;
			}
		}
	}
	
	@Util
	public static void AsignarAgente(Long idSolicitud, Autorizaciones autorizaciones) {
		boolean encontrado = false;
		List<Agente> listaAgentes = Agente.findAll();
		for (Agente ag : listaAgentes) {
			if (ag.username.equals(autorizaciones.nip.valor)){
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
			ag.username = autorizaciones.nip.valor;
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
	
	@Util
	public static void crearRender(Long idSolicitud, Long idAutorizaciones) {
		if (!Messages.hasMessages()) {

			Messages.ok("Página creada correctamente");
			Messages.keep();
			redirect("AutorizacionController.index", "editar", idSolicitud, idAutorizaciones);

		}
		Messages.keep();
		redirect("PaginaAutorizarController.index", "crear", idSolicitud, idAutorizaciones);
	}
	
	@Util
	public static void borrarRender(Long idSolicitud, Long idAutorizaciones) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página borrada correctamente++");
			Messages.keep();
			redirect("AutorizacionController.index", "editar", idSolicitud, idAutorizaciones);
		}
		Messages.keep();
		redirect("PaginaAutorizarController.index", "borrar", idSolicitud, idAutorizaciones);
	}
	
	@Util
	public static void editarRender(Long idSolicitud, Long idAutorizaciones) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("AutorizacionController.index", "editar", idSolicitud, idAutorizaciones);
		}
		Messages.keep();
		redirect("PaginaAutorizarController.index", "editar", idSolicitud, idAutorizaciones);
	}
	
}
