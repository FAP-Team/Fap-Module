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

public class DesistimientoPresentarController extends DesistimientoPresentarControllerGen {
	private final static String REDIRECT_DESISTIMIENTO = "DesistimientoController.index";
	private final static String REDIRECT_DESISTIMIENTOPRESENTAR = "DesistimientoPresentarController.index";
	
	public static void tablatablaFirmantesHecho(Long idDesistimiento) {

		java.util.List<Firmante> rows = Firmante.find("select firmante from Solicitud solicitud join solicitud.desistimiento.registro.firmantes firmante where solicitud.id=? and firmante.tipo = ? and firmante.fechaFirma is not null", idDesistimiento, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows;

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "fechaFirma", "id"));
	}
	
	public static void formFirmaPF(Long idSolicitud, Long idDesistimiento, String firma, String firmarRegistrarNif) {
		checkAuthenticity();
		SolicitudGenerica dbSolicitud = DesistimientoPresentarController.getSolicitudGenerica(idSolicitud);
		Desistimiento dbDesistimiento = DesistimientoPresentarController.getDesistimiento(idSolicitud, idDesistimiento);
		
		if ((permisoFormFirmaPF("editar")) || (permisoFormFirmaPF("crear"))) {
			TramiteDesistimiento tramite = new TramiteDesistimiento(dbSolicitud);
			tramite.firmar(firma);
			if (!Messages.hasErrors()) {
				try {
					tramite.registrar();
					dbSolicitud.estado = "desistido";
					dbSolicitud.save();
				} catch (RegistroServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else{
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (firmarRegistrarNif != null) {
			DesistimientoPresentarController.firmarRegistrarNifFormFirmaPF(idSolicitud, idDesistimiento, firma);
			DesistimientoPresentarController.formFirmaPFRender(idSolicitud, idDesistimiento);
		}

		if (!Messages.hasErrors()) {
			DesistimientoPresentarController.formFirmaPFValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada con éxito");
			redirect(REDIRECT_DESISTIMIENTO, "all", idSolicitud, idDesistimiento);
		} else
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		DesistimientoPresentarController.formFirmaPFRender(idSolicitud, idDesistimiento);
	}

	
	public static void formFirmaRepresentante(Long idSolicitud, Long idDesistimiento, String firma, String firmarRepresentante) {
		checkAuthenticity();
		SolicitudGenerica dbSolicitud = DesistimientoPresentarController.getSolicitudGenerica(idSolicitud);
		Desistimiento dbDesistimiento = DesistimientoPresentarController.getDesistimiento(idSolicitud, idDesistimiento);
		
		if ((permisoFormFirmaRepresentante("editar")) || (permisoFormFirmaRepresentante("crear"))){
			TramiteDesistimiento tramite = new TramiteDesistimiento(dbSolicitud);
			tramite.firmar(firma);
		}else{
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}


		if (firmarRepresentante != null) {
			DesistimientoPresentarController.firmarRepresentanteFormFirmaRepresentante(idSolicitud, idDesistimiento, firma);
			DesistimientoPresentarController.formFirmaRepresentanteRender(idSolicitud, idDesistimiento);
		}

		if (!Messages.hasErrors()) {
			DesistimientoPresentarController.formFirmaRepresentanteValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada con éxito");
			redirect(REDIRECT_DESISTIMIENTOPRESENTAR, "all", dbSolicitud.id, idDesistimiento);
		} else
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		DesistimientoPresentarController.formFirmaRepresentanteRender(idSolicitud, idDesistimiento);
	}
	
	
	public static void formFirmaCif(Long idSolicitud, Long idDesistimiento, String firma, String firmarRegistrarCif) {
		checkAuthenticity();
		SolicitudGenerica dbSolicitud = DesistimientoPresentarController.getSolicitudGenerica(idSolicitud);
		Desistimiento dbDesistimiento = DesistimientoPresentarController.getDesistimiento(idSolicitud, idDesistimiento);
		
		if ((permisoFormFirmaCif("editar")) || (permisoFormFirmaCif("crear"))) {
			TramiteDesistimiento tramite = new TramiteDesistimiento(dbSolicitud);
			tramite.firmar(firma);
			
			//TODO SMB Añadido registrar
			if (!Messages.hasErrors()) {
				try {
					tramite.registrar();
				} catch (RegistroServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (firmarRegistrarCif != null) {
			DesistimientoPresentarController.firmarRegistrarCifFormFirmaCif(idSolicitud, idDesistimiento, firma);
			DesistimientoPresentarController.formFirmaCifRender(idSolicitud, idDesistimiento);
		}

		if (!Messages.hasErrors()) {
			DesistimientoPresentarController.formFirmaCifValidateRules(firma);
		}
		Messages.keep();
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada con éxito");
			redirect(REDIRECT_DESISTIMIENTO, "all", dbSolicitud.id, idDesistimiento);
		} else
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		DesistimientoPresentarController.formFirmaCifRender(idSolicitud, idDesistimiento);
	}
	
	
	public static void frmRegistrar(Long idSolicitud, Long idDesistimiento, String botonRegistrar) {
		checkAuthenticity();
		SolicitudGenerica dbSolicitud = DesistimientoPresentarController.getSolicitudGenerica(idSolicitud);
		
		if ((permisoFormFirmaCif("editar")) || (permisoFormFirmaCif("crear"))) {
			TramiteDesistimiento tramite = new TramiteDesistimiento(dbSolicitud);
			try {
				tramite.registrar();
			} catch (RegistroServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {
			DesistimientoPresentarController.frmRegistrarValidateRules();
		}
		Messages.keep();
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada con éxito");
			redirect(REDIRECT_DESISTIMIENTO, "all", dbSolicitud.id, idDesistimiento);
		} else
			log.info("Acción Editar de página: " + "gen/DesistimientoPresentar/DesistimientoPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
			DesistimientoPresentarController.frmRegistrarRender(idSolicitud, idDesistimiento);
	}
	
}
