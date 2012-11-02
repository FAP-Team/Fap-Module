package controllers;

import java.util.List;
import java.util.Map;

import play.mvc.Util;
import services.RegistroServiceException;
import tramitacion.TramiteAceptacionRenuncia;

import messages.Messages;
import models.Firmante;
import models.SolicitudGenerica;
import controllers.fap.PresentacionFapController;
import controllers.fap.ResolucionFapController;
import controllers.gen.AceptacionRenunciaPresentarControllerGen;
import emails.Mails;

public class AceptacionRenunciaPresentarController extends AceptacionRenunciaPresentarControllerGen {
	
	public static void tablatablaFirmantesHecho(Long idSolicitud) {

		java.util.List<Firmante> rows = Firmante.find("select firmante from SolicitudGenerica solicitud join solicitud.aceptarRenunciar.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is not null", idSolicitud, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "fechaFirma", "id"));
	}

	public static void tablatablaFirmantesEspera(Long idSolicitud) {

		java.util.List<Firmante> rows = Firmante.find("select firmante from SolicitudGenerica solicitud join solicitud.aceptarRenunciar.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is null", idSolicitud, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "id"));
	}
	
	@Util
	public static void formFirma(Long idSolicitud, String firma, String firmarRegistrarNif) {
		checkAuthenticity();
		if (!permisoFormFirma("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = AceptacionRenunciaPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAceptacionRenuncia.firmar(firma);
			try {
				trAceptacionRenuncia.registrar();
				try {
					ResolucionFapController.invoke(ResolucionFapController.class, "setEstadoAfterResolucion", idSolicitud);
					dbSolicitud.save();
				} catch (Throwable e) {
					play.Logger.error("Hubo un problema al intentar cambiar el estado de la solicitud: "+idSolicitud+", en Aceptación Renuncia"+e.getMessage());
				}
			} catch (RegistroServiceException e) {
				e.printStackTrace();
			}
		}

		if (!Messages.hasErrors()) {
			AceptacionRenunciaPresentarController.formFirmaValidateRules(firma);
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AceptacionRenunciaPresentarController.formFirmaRender(idSolicitud);
	}
	
	@Util
	public static void formFirmaRender(Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PaginaAceptacionRenunciaController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("AceptacionRenunciaPresentarController.index", "editar", idSolicitud);
	}
	
	@Util
	public static void formFirmaRepresentante(Long idSolicitud, String firma, String firmarRepresentante) {
		checkAuthenticity();
		if (!permisoFormFirmaRepresentante("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AceptacionRenunciaPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAceptacionRenuncia.firmar(firma);
		}

		if (!Messages.hasErrors()) {
			AceptacionRenunciaPresentarController.formFirmaRepresentanteValidateRules(firma);
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AceptacionRenunciaPresentarController.formFirmaRepresentanteRender(idSolicitud);
	}
	
	@Util
	public static void formFirmaCif(Long idSolicitud, String firma, String firmarRegistrarCif) {
		checkAuthenticity();
		if (!permisoFormFirmaCif("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AceptacionRenunciaPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);
		
		if (!Messages.hasErrors()) {
			trAceptacionRenuncia.firmar(firma);
			try {
				trAceptacionRenuncia.registrar();
				try {
					ResolucionFapController.invoke(ResolucionFapController.class, "setEstadoAfterResolucion", idSolicitud);
					dbSolicitud.save();
				} catch (Throwable e) {
					play.Logger.error("Hubo un problema al intentar cambiar el estado de la solicitud: "+idSolicitud+", en Aceptación Renuncia");
				}
			} catch (RegistroServiceException e) {
				e.printStackTrace();
			}
		}

		if (!Messages.hasErrors()) {
			AceptacionRenunciaPresentarController.formFirmaCifValidateRules(firma);
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		AceptacionRenunciaPresentarController.formFirmaCifRender(idSolicitud);
	}
	
	@Util
	public static void formRegistrar(Long idSolicitud) {
		checkAuthenticity();
		if (!permisoFormRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AceptacionRenunciaPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			try {
				trAceptacionRenuncia.registrar();
				try {
					ResolucionFapController.invoke(ResolucionFapController.class, "setEstadoAfterResolucion", idSolicitud);
					dbSolicitud.save();
				} catch (Throwable e) {
					play.Logger.error("Hubo un problema al intentar cambiar el estado de la solicitud: "+idSolicitud+", en Aceptación Renuncia");
				}
			} catch (RegistroServiceException e) {
				e.printStackTrace();
			}
		}

		if (!Messages.hasErrors()) {
			AceptacionRenunciaPresentarController.formRegistrarValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AceptacionRenunciaPresentar/AceptacionRenunciaPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AceptacionRenunciaPresentarController.formRegistrarRender(idSolicitud);
	}
	
}
