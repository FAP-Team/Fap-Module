package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.ComunicacionInterna;
import models.ReturnUnidadOrganicaFap;
import models.SolicitudGenerica;
import play.mvc.Util;
import services.ComunicacionesInternasServiceException;
import services.comunicacionesInternas.ComunicacionesInternasServiceImpl;
import tags.ComboItem;
import utils.ComunicacionesInternasUtils;
import validation.CustomValidation;

import com.google.gson.Gson;

import controllers.fap.AgenteController;
import controllers.gen.PaginaDatosNuevaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PaginaDatosNuevaComunicacionInternaController extends PaginaDatosNuevaComunicacionInternaControllerGen {
	
	@Inject
	protected static ComunicacionesInternasServiceImpl ciService;
	
	public static void index(String accion, Long idSolicitud, Long idComunicacionInterna) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/PaginaDatosNuevaComunicacionInterna/PaginaDatosNuevaComunicacionInterna.html");
		}

		SolicitudGenerica solicitud = PaginaDatosNuevaComunicacionInternaController.getSolicitudGenerica(idSolicitud);

		ComunicacionInterna comunicacionInterna = null;
		if ("crear".equals(accion)) {
			comunicacionInterna = PaginaDatosNuevaComunicacionInternaController.getComunicacionInterna();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				comunicacionInterna.save();
				idComunicacionInterna = comunicacionInterna.id;
				solicitud.comunicacionesInternas.add(comunicacionInterna);
				solicitud.save();

				accion = "editar";
			}

		} else 
			if (!"borrado".equals(accion)) {
				comunicacionInterna = PaginaDatosNuevaComunicacionInternaController.getComunicacionInterna(idSolicitud, idComunicacionInterna);
				if ("enviada".equals(comunicacionInterna.estado)) {
					Messages.info("La comunicación interna ya ha sido enviada");
					Messages.keep();
					redirect("ComunicacionesInternasController.index", "editar", idSolicitud, idComunicacionInterna);
				}
			}
					
		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/PaginaDatosNuevaComunicacionInterna/PaginaDatosNuevaComunicacionInterna.html" + " Agente: " + logAgente);
		renderTemplate("fap/PaginaDatosNuevaComunicacionInterna/PaginaDatosNuevaComunicacionInterna.html", accion, idSolicitud, idComunicacionInterna, solicitud, comunicacionInterna);
	}
	
	@Util
	public static void crearRender(Long idSolicitud, Long idComunicacionInterna) {
		if (!Messages.hasMessages()) {

			Messages.ok("Página creada correctamente");
			Messages.keep();
			redirect("PaginaNuevaComunicacionInternaDocumentosController.index", "editar", idSolicitud, idComunicacionInterna);

		}
		Messages.keep();
		redirect("PaginaDatosNuevaComunicacionInternaController.index", "crear", idSolicitud, idComunicacionInterna);
	}
	
//	@Util
//	public static void editarRender(Long idSolicitud, Long idComunicacionInterna) {
//		ComunicacionInterna comunicacionInterna = getComunicacionInterna(idSolicitud, idComunicacionInterna);
//		
//		if (!Messages.hasErrors()) {
//			Messages.ok("Página editada correctamente");
//			Messages.keep();
//			if ("creada".equals(comunicacionInterna.estado)){
//				redirect("PaginaNuevaComunicacionInternaDocumentosController.index", "editar", idSolicitud, idComunicacionInterna);
//			} else
//				if ("docAdjuntos".equals(comunicacionInterna.estado) || "datosCompletos".equals(comunicacionInterna.estado)){
//					redirect("PaginaAltaComunicacionInternaController.index", "editar", idSolicitud, idComunicacionInterna);
//				} else {
//					Messages.info("La comunicación interna ya ha sido enviada");
//					Messages.keep();
//					redirect("ComunicacionesInternasController.index", "editar", idSolicitud, idComunicacionInterna);
//				}
//		}
//		Messages.keep();
//		redirect("PaginaDatosNuevaComunicacionInternaController.index", "editar", idSolicitud, idComunicacionInterna);
//	}

	@Util
	public static void PaginaDatosNuevaComunicacionInternaValidateCopy(String accion, ComunicacionInterna dbComunicacionInterna, ComunicacionInterna comunicacionInterna) {
		CustomValidation.clearValidadas();

		CustomValidation.valid("comunicacionInterna.asiento", comunicacionInterna.asiento);
		CustomValidation.valid("comunicacionInterna", comunicacionInterna);
		CustomValidation.required("comunicacionInterna.asiento.interesado", comunicacionInterna.asiento.interesado);
		dbComunicacionInterna.asiento.interesado = comunicacionInterna.asiento.interesado;
		CustomValidation.required("comunicacionInterna.asiento.resumen", comunicacionInterna.asiento.resumen);
		dbComunicacionInterna.asiento.resumen = comunicacionInterna.asiento.resumen;
		
		CustomValidation.valid("comunicacionInterna.asiento.unidadOrganicaDestino", comunicacionInterna.asiento.unidadOrganicaDestino);
		CustomValidation.required("comunicacionInterna.asiento.unidadOrganicaDestino.codigo", comunicacionInterna.asiento.unidadOrganicaDestino.codigo);
		Long codigo = comunicacionInterna.asiento.unidadOrganicaDestino.codigo;
		dbComunicacionInterna.asiento.unidadOrganicaDestino =getUnidadOrganicaFAP(codigo);
		dbComunicacionInterna.asiento.unidadOrganicaDestino.codigo = codigo;
		
		dbComunicacionInterna.asiento.userId = ciService.USUARIOHIPERREG;
		dbComunicacionInterna.asiento.password = ciService.PASSWORDHIPERREG;
		dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.creada.name();
	}
	
	public static String uoDestinoJerarquia(int codigo, int subnivel){
		List<ReturnUnidadOrganicaFap> lstUO = null;
		List<ReturnUnidadOrganicaFap> lstUOSubNivel = null;
		String resultados = null;

		try {
			lstUO = ciService.obtenerUnidadesOrganicas((long) codigo);
			
			if (lstUO != null){
				ComunicacionesInternasUtils.cargarUnidadesOrganicas(lstUO);
				lstUOSubNivel = new ArrayList<ReturnUnidadOrganicaFap>();
				for (ReturnUnidadOrganicaFap unidad : lstUO){
					if (ComunicacionesInternasUtils.calcularNivelUO(unidad) == subnivel)
						lstUOSubNivel.add(unidad);
				}
				
				if (lstUOSubNivel != null)
					resultados = new Gson().toJson(lstUOSubNivel);
			}
		} catch (ComunicacionesInternasServiceException e) {
			log.error("No se han podido obtener las Unidades OrgÃ¡nicas del subnivel: "+subnivel);
		}
		
		return resultados;
	}
	
	public static ReturnUnidadOrganicaFap getUnidadOrganicaFAP(Long codUnidadOrganica){
		ReturnUnidadOrganicaFap unidad = null;
		if (codUnidadOrganica == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parÃ¡metro codUnidadOrganica"))
				Messages.fatal("Falta parÃ¡metro codUnidadOrganica");
		} else {
			unidad = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", codUnidadOrganica).first();
			if (unidad == null) {
				Messages.fatal("Error al recuperar Unidad OrgÃ¡nica");
			}
		}
		return unidad;
	}
	
}
