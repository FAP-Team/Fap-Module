package controllers;

import java.util.List;
import java.util.Map;

import play.mvc.Util;

import services.RegistroServiceException;
import tramitacion.TramiteBase;
import tramitacion.TramiteDesistimiento;

import messages.Messages;
import models.Desistimiento;
import models.Firmante;
import models.SolicitudGenerica;
import controllers.gen.DesistimientoPresentarControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;

public class DesistimientoPresentarController extends DesistimientoPresentarControllerGen {
	private final static String REDIRECT_DESISTIMIENTO = "DesistimientoController.index";
	private final static String REDIRECT_DESISTIMIENTOPRESENTAR = "DesistimientoPresentarController.index";
	
	public static void tablatablaFirmantesHecho(Long idDesistimiento) {

		java.util.List<Firmante> rows = Firmante.find("select firmante from Solicitud solicitud join solicitud.desistimiento.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is not null", idDesistimiento, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows;

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "fechaFirma", "id"));
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaPF(Long idSolicitud, Long idDesistimiento, String firma, String firmarRegistrarNif) {
		checkAuthenticity();
		if (!permisoFormFirmaPF("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = DesistimientoPresentarController.getSolicitudGenerica(idSolicitud);
		Desistimiento dbDesistimiento = DesistimientoPresentarController.getDesistimiento(idSolicitud, idDesistimiento);

		if (!Messages.hasErrors()) {
			TramiteDesistimiento tramite = new TramiteDesistimiento(dbSolicitud);
			tramite.firmar(firma);
			if (!Messages.hasErrors()) {
				try {
					tramite.registrar();
					dbSolicitud.estado = EstadosSolicitudEnum.desistido.name();
					dbSolicitud.save();
				} catch (RegistroServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (!Messages.hasErrors()) {
			DesistimientoPresentarController.formFirmaPFValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada con éxito");
			redirect(REDIRECT_DESISTIMIENTO, "editar", idSolicitud, idDesistimiento);
		} else
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		DesistimientoPresentarController.formFirmaPFRender(idSolicitud, idDesistimiento);
	}
		
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaRepresentante(Long idSolicitud, Long idDesistimiento, String firma, String firmarRepresentante) {
		checkAuthenticity();
		if (!permisoFormFirmaRepresentante("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = DesistimientoPresentarController.getSolicitudGenerica(idSolicitud);
		Desistimiento dbDesistimiento = DesistimientoPresentarController.getDesistimiento(idSolicitud, idDesistimiento);


		if (!Messages.hasErrors()) {
			TramiteDesistimiento tramite = new TramiteDesistimiento(dbSolicitud);
			tramite.firmar(firma);
			if (!Messages.hasErrors()){ //Si no hubo errores firmando
				//Si han firmado todos -> firmado el desistimiento
				if (dbSolicitud.desistimiento.registro.fasesRegistro.firmada){ //Si no hubo errores firmando
					dbSolicitud.estado = EstadosSolicitudEnum.desistido.name();
					dbSolicitud.save();
				}
			}
		}

		if (!Messages.hasErrors()) {
			DesistimientoPresentarController.formFirmaRepresentanteValidateRules(firma);
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada con éxito");
			redirect(REDIRECT_DESISTIMIENTOPRESENTAR, "editar", dbSolicitud.id, idDesistimiento);
		} else
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		DesistimientoPresentarController.formFirmaRepresentanteRender(idSolicitud, idDesistimiento);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmRegistrar(Long idSolicitud, Long idDesistimiento, String botonRegistrar) {
		checkAuthenticity();
		if (!permisoFrmRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = DesistimientoPresentarController.getSolicitudGenerica(idSolicitud);

		if (!Messages.hasErrors()) {
			TramiteDesistimiento tramite = new TramiteDesistimiento(dbSolicitud);
			try {
				tramite.registrar();
			} catch (RegistroServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!Messages.hasErrors()) {
			DesistimientoPresentarController.frmRegistrarValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		DesistimientoPresentarController.frmRegistrarRender(idSolicitud, idDesistimiento);
	}
}
