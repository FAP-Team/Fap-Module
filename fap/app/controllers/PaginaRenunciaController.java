package controllers;

import messages.Messages;
import models.SolicitudGenerica;
import play.mvc.Util;
import tramitacion.TramiteRenuncia;
import controllers.gen.PaginaRenunciaControllerGen;

public class PaginaRenunciaController extends PaginaRenunciaControllerGen {
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void prepararFirmar(Long idSolicitud, Long idRenuncia, String botonPrepararFirmar) {
		checkAuthenticity();
		if (!permisoPrepararFirmar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = PaginaRenunciaController.getSolicitudGenerica(idSolicitud);
		TramiteRenuncia trRenuncia = new TramiteRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trRenuncia.prepararFirmar();
		}

		if (!Messages.hasErrors()) {
			PaginaRenunciaController.prepararFirmarValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaRenuncia/PaginaRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaRenuncia/PaginaRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaRenunciaController.prepararFirmarRender(idSolicitud);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void deshacer(Long idSolicitud, String botonModificar) {
		checkAuthenticity();
		if (!permisoDeshacer("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = PaginaRenunciaController.getSolicitudGenerica(idSolicitud);
		TramiteRenuncia trRenuncia = new TramiteRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trRenuncia.deshacer();
		}

		if (!Messages.hasErrors()) {
			PaginaRenunciaController.deshacerValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaRenuncia/PaginaRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaRenuncia/PaginaRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaRenunciaController.deshacerRender(idSolicitud);
	}
}
