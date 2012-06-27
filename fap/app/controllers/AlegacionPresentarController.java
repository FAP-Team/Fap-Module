package controllers;

import java.util.List;
import java.util.Map;

import play.mvc.Util;
import services.RegistroServiceException;
import tramitacion.TramiteAlegacion;

import messages.Messages;
import models.Firmante;
import models.SolicitudGenerica;
import controllers.gen.AlegacionPresentarControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadoAlegacionEnum;

public class AlegacionPresentarController extends AlegacionPresentarControllerGen {
	public static void tablatablaFirmantesHecho(Long idSolicitud) {

		java.util.List<Firmante> rows = Firmante.find("select firmante from SolicitudGenerica solicitud join solicitud.alegaciones.actual.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is not null", idSolicitud, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "fechaFirma", "id"));
	}

	public static void tablatablaFirmantesEspera(Long idSolicitud) {

		java.util.List<Firmante> rows = Firmante.find("select firmante from SolicitudGenerica solicitud join solicitud.alegaciones.actual.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is null", idSolicitud, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "id"));
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaPF(Long idSolicitud, String firma, String firmarRegistrarNif) {
		checkAuthenticity();
		if (!permisoFormFirmaPF("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AlegacionPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAlegacion trAlegacion = new TramiteAlegacion(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAlegacion.firmar(firma);
			try {
				trAlegacion.registrar();
				dbSolicitud.alegaciones.actual.estado = EstadoAlegacionEnum.iniciada.toString();
				try {
					Mails.enviar(trAlegacion.getMail(), dbSolicitud);
				} catch (Exception e) {
					play.Logger.error("No se pudo enviar mail de aceptación " + trAlegacion.getMail() + " de la solicitud " + dbSolicitud.id);
				}
			} catch (RegistroServiceException e) {
				e.printStackTrace();
			}
		}

		if (!Messages.hasErrors()) {
			AlegacionPresentarController.formFirmaPFValidateRules(firma);
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AlegacionPresentarController.formFirmaPFRender(idSolicitud);
	}
	
	@Util
	public static void formFirmaPFRender(Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("AlegacionesController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("AlegacionPresentarController.index", "editar", idSolicitud);
	}
	
	@Util
	public static void formFirmaRepresentante(Long idSolicitud, String firma, String firmarRepresentante) {
		checkAuthenticity();
		if (!permisoFormFirmaRepresentante("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AlegacionPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAlegacion trAlegacion = new TramiteAlegacion(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAlegacion.firmar(firma);
		}

		if (!Messages.hasErrors()) {
			AlegacionPresentarController.formFirmaRepresentanteValidateRules(firma);
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AlegacionPresentarController.formFirmaRepresentanteRender(idSolicitud);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaCif(Long idSolicitud, String firma, String firmarRegistrarCif) {
		checkAuthenticity();
		if (!permisoFormFirmaCif("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AlegacionPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAlegacion trAlegacion = new TramiteAlegacion(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAlegacion.firmar(firma);
			try {
				trAlegacion.registrar();
				dbSolicitud.alegaciones.actual.estado = EstadoAlegacionEnum.iniciada.toString();
				try {
					Mails.enviar(trAlegacion.getMail(), dbSolicitud);
				} catch (Exception e) {
					play.Logger.error("No se pudo enviar mail de aceptación " + trAlegacion.getMail() + " de la solicitud " + dbSolicitud.id);
				}
			} catch (RegistroServiceException e) {
				e.printStackTrace();
			}
		}

		if (!Messages.hasErrors()) {
			AlegacionPresentarController.formFirmaCifValidateRules(firma);
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AlegacionPresentarController.formFirmaCifRender(idSolicitud);
	}
	
	@Util
	public static void formFirmaCifRender(Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("AlegacionesController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("AlegacionPresentarController.index", "editar", idSolicitud);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmRegistrar(Long idSolicitud, String botonRegistrar) {
		checkAuthenticity();
		if (!permisoFrmRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = AlegacionPresentarController.getSolicitudGenerica(idSolicitud);
		TramiteAlegacion trAlegacion = new TramiteAlegacion(dbSolicitud);

		if (!Messages.hasErrors()) {
			try {
				trAlegacion.registrar();
				dbSolicitud.alegaciones.actual.estado = EstadoAlegacionEnum.iniciada.toString();
				try {
					Mails.enviar(trAlegacion.getMail(), dbSolicitud);
				} catch (Exception e) {
					play.Logger.error("No se pudo enviar mail de aceptación " + trAlegacion.getMail() + " de la solicitud " + dbSolicitud.id);
				}
			} catch (RegistroServiceException e) {
				e.printStackTrace();
			}
		}

		if (!Messages.hasErrors()) {
			AlegacionPresentarController.frmRegistrarValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AlegacionPresentar/AlegacionPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		
		AlegacionPresentarController.frmRegistrarRender(idSolicitud);
	}
	
	@Util
	public static void frmRegistrarRender(Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("AlegacionesController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("AlegacionPresentarController.index", "editar", idSolicitud);
	}
	
}
