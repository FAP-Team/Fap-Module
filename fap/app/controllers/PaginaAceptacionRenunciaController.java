package controllers;

import java.util.List;

import messages.Messages;
import models.SolicitudGenerica;
import play.mvc.Util;
import tramitacion.Documentos;
import tramitacion.TramiteAceptacionRenuncia;
import controllers.gen.PaginaAceptacionRenunciaControllerGen;

public class PaginaAceptacionRenunciaController extends PaginaAceptacionRenunciaControllerGen {
	
	@Util
	public static void formAceptarRenunciar(Long idSolicitud, SolicitudGenerica solicitud) {
		checkAuthenticity();
		if (!permisoFormAceptarRenunciar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);
		PaginaAceptacionRenunciaController.formAceptarRenunciarBindReferences(dbSolicitud);

		if (!Messages.hasErrors()) {
			
			if (dbSolicitud.aceptarRenunciar.seleccion != null) {
				if (dbSolicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("acepta"))
					dbSolicitud.aceptarRenunciar.motivoRenuncia = "";
				play.Logger.info("sdsdfsdfsd");
				if (dbSolicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("renuncia")) {
//					Documentos.borrarDocumento(dbSolicitud.aceptarRenunciar, "documentos");
					for (int i = 0; i < dbSolicitud.aceptarRenunciar.documentos.size(); i++) {
						dbSolicitud.aceptarRenunciar.documentos.remove(i);
					}
				}
			}
			
			PaginaAceptacionRenunciaController.formAceptarRenunciarValidateCopy("editar", dbSolicitud, solicitud);

		}

		if (!Messages.hasErrors()) {
			PaginaAceptacionRenunciaController.formAceptarRenunciarValidateRules(dbSolicitud, solicitud);
		}
		
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaAceptacionRenunciaController.formAceptarRenunciarRender(idSolicitud);
	}
	
	@Util
	public static void prepararFirmar(Long idSolicitud, String botonPrepararFirmar) {
		checkAuthenticity();
		if (!permisoPrepararFirmar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAceptacionRenuncia.prepararFirmar();
		}

		if (!Messages.hasErrors()) {
			PaginaAceptacionRenunciaController.prepararFirmarValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaAceptacionRenunciaController.prepararFirmarRender(idSolicitud);
	}
	
	@Util
	public static void deshacer(Long idSolicitud, String botonModificar) {
		checkAuthenticity();
		if (!permisoDeshacer("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAceptacionRenuncia.deshacer();
		}

		if (!Messages.hasErrors()) {
			PaginaAceptacionRenunciaController.deshacerValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaAceptacionRenunciaController.deshacerRender(idSolicitud);
	}
	
	@Util
	public static void prepararFirmarRender(Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("AceptacionRenunciaPresentarController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("PaginaAceptacionRenunciaController.index", "editar", idSolicitud);
	}
	
}
