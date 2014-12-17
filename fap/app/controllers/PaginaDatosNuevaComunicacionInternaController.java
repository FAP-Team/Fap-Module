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
import properties.FapProperties;
import services.ServiciosGenericosService;
import tags.ComboItem;
import utils.ComunicacionesInternasUtils;
import utils.ServiciosGenericosUtils;
import validation.CustomValidation;

import com.google.gson.Gson;

import controllers.fap.AgenteController;
import controllers.gen.PaginaDatosNuevaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PaginaDatosNuevaComunicacionInternaController extends PaginaDatosNuevaComunicacionInternaControllerGen {
	
	@Inject
	protected static ServiciosGenericosService genericosService;
	
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
		
		dbComunicacionInterna.asiento.userId = FapProperties.get("fap.platino.registro.username");
		dbComunicacionInterna.asiento.password = FapProperties.get("fap.platino.registro.password");
		dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.creada.name();
	}
	
	public static String uoDestinoJerarquia(int codigo, int subnivel){
		List<ReturnUnidadOrganicaFap> lstUO = null;
		List<ReturnUnidadOrganicaFap> lstUOSubNivel = null;
		List<ComboItem> lstCombo = new ArrayList<ComboItem>();
		String resultados = null;

		lstUO = genericosService.obtenerUnidadesOrganicas((long) codigo, 
				FapProperties.get("fap.platino.registro.username"),
				FapProperties.get("fap.platino.registro.password"));
		
		if (lstUO != null){
			ServiciosGenericosUtils.cargarUnidadesOrganicas(lstUO);
			lstUOSubNivel = new ArrayList<ReturnUnidadOrganicaFap>();
			for (ReturnUnidadOrganicaFap unidad : lstUO){
				if (ServiciosGenericosUtils.calcularNivelUO(unidad) == subnivel)
					lstUOSubNivel.add(unidad);
			}
			
			if (lstUOSubNivel != null) {
				for (ReturnUnidadOrganicaFap unidad: lstUOSubNivel)
					lstCombo.add(new ComboItem(unidad.codigo, unidad.codigoCompleto + " "  + unidad.descripcion));
					
				resultados = new Gson().toJson(lstCombo);
			}
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
