package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.AsientoAmpliadoCIFap;
import models.AsientoCIFap;
import models.ComunicacionInterna;
import models.Documento;
import models.ListaUris;
import models.ReturnUnidadOrganicaFap;
import models.SolicitudGenerica;
import play.mvc.Util;
import properties.FapProperties;
import services.ServiciosGenericosService;
import services.platino.PlatinoBDOrganizacionServiceImpl;
import tags.ComboItem;
import utils.ComunicacionesInternasUtils;
import utils.ServiciosGenericosUtils;
import validation.CustomValidation;

import com.google.gson.Gson;

import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PaginaDatosNuevaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;

public class PaginaDatosNuevaComunicacionInternaController extends PaginaDatosNuevaComunicacionInternaControllerGen {
	
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
			
			if (comunicacionInterna.asiento == null)
				comunicacionInterna.asiento = new AsientoCIFap();
			
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

	@Util
	public static void PaginaDatosNuevaComunicacionInternaValidateCopy(String accion, ComunicacionInterna dbComunicacionInterna, ComunicacionInterna comunicacionInterna) {
		CustomValidation.clearValidadas();

		CustomValidation.valid("comunicacionInterna.asiento", comunicacionInterna.asiento);
		CustomValidation.valid("comunicacionInterna", comunicacionInterna);
		
		if (dbComunicacionInterna.asiento == null)
			dbComunicacionInterna.asiento = new AsientoCIFap();
		
		dbComunicacionInterna.asiento.interesado = comunicacionInterna.asiento.interesado;
		CustomValidation.valid("comunicacionInterna.asiento.unidadOrganicaDestino", comunicacionInterna.asiento.unidadOrganicaDestino);
		CustomValidation.required("comunicacionInterna.asiento.unidadOrganicaDestino.codigo", comunicacionInterna.asiento.unidadOrganicaDestino.codigo);
		Long codigoUODestino = comunicacionInterna.asiento.unidadOrganicaDestino.codigo;
		if (codigoUODestino != null) {
			dbComunicacionInterna.asiento.unidadOrganicaDestino = getUnidadOrganicaFAP(codigoUODestino);
			dbComunicacionInterna.asiento.unidadOrganicaDestino.codigo = codigoUODestino;
		}
		CustomValidation.required("comunicacionInterna.asiento.resumen", comunicacionInterna.asiento.resumen);
		dbComunicacionInterna.asiento.resumen = comunicacionInterna.asiento.resumen;
		dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.creada.name();
	}
	
	public static String uoDestinoJerarquia(int codigo, int subnivel){
		List<ReturnUnidadOrganicaFap> lstUO = null;
		List<ReturnUnidadOrganicaFap> lstUOSubNivel = null;
		List<ComboItem> lstCombo = new ArrayList<ComboItem>();
		String resultados = null;
		
		if (!Messages.hasErrors()) {
			lstUO = ServiciosGenericosUtils.obtenerUnidadesOrganicasBD((long) codigo);
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
	
//	private static void obtenerUoOrigen(ComunicacionInterna comunicacionInterna, Agente agente){
//		PlatinoBDOrganizacionServiceImpl platinoDBOrgPort = InjectorConfig.getInjector().getInstance(PlatinoBDOrganizacionServiceImpl.class);
//		try {
//			String uri = platinoDBOrgPort.recuperarURIPersona(agente.usuarioldap);		
//			DatosBasicosPersonaItem persona = platinoDBOrgPort.recuperarDatosPersona(uri);
//			ReturnUnidadOrganicaFap unidad = null;
//			Long id = (long) 9938;
		
//			if (persona != null && persona.getCodigoUnidadOrg() != null && !persona.getCodigoUnidadOrg().isEmpty()){
//				Long codigobdorganizacion = Long.parseLong(persona.getCodigoUnidadOrg());
//				unidad = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", id).first();
			
//				if (unidad != null)
//					comunicacionInterna.asiento.unidadOrganicaOrigen = unidad;
//			}
//		} catch (DBOrganizacionException_Exception e) {
//			log.error("No se puede obtener la unidad orgánica de origen del solicitante: " + e.getMessage());
//		}
//	}
	
}
