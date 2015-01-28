package controllers;

import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.Agente;
import models.ComunicacionInterna;
import models.ReturnComunicacionInternaFap;
import play.mvc.Util;
import services.ComunicacionesInternasService;
import services.ComunicacionesInternasServiceException;
import services.platino.PlatinoBDOrganizacionServiceImpl;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PaginaAltaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaCriteriaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;

public class PaginaAltaComunicacionInternaController extends PaginaAltaComunicacionInternaControllerGen {
	
	@Inject
	protected static ComunicacionesInternasService ciService;
	
//	@Util
//	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
//	public static void frmNuevoAsiento(Long idSolicitud, Long idComunicacionInterna, ComunicacionInterna comunicacionInterna, String btnNuevoAsiento) {
//		checkAuthenticity();
//		if (!permisoFrmNuevoAsiento("editar")) {
//			Messages.error("No tiene permisos suficientes para realizar la acción");
//		}
//		ComunicacionInterna dbComunicacionInterna = PaginaAltaComunicacionInternaController.getComunicacionInterna(idSolicitud, idComunicacionInterna);
//
//		PaginaAltaComunicacionInternaController.frmNuevoAsientoBindReferences(comunicacionInterna);
//
//		if (!Messages.hasErrors()) {
//			PaginaAltaComunicacionInternaController.frmNuevoAsientoValidateCopy("editar", dbComunicacionInterna, comunicacionInterna);
//		}
//
//		if (!Messages.hasErrors()) {
//			PaginaAltaComunicacionInternaController.frmNuevoAsientoValidateRules(dbComunicacionInterna, comunicacionInterna);
//		}
//		
//		if (!Messages.hasErrors()){
//			if (AgenteController.getAgente().usuarioldap == null || AgenteController.getAgente().usuarioldap.isEmpty())
//				Messages.error("No se dispone de un usuario para la Base de Datos de Organización (usuarioldap)");
//		}
//		
//		if (!Messages.hasErrors()) {
//			try {
//				ReturnComunicacionInternaFap respuesta = ciService.crearNuevoAsiento(dbComunicacionInterna.asiento);
//				
////				PlatinoBDOrganizacionServiceImpl platinoDBOrgPort = InjectorConfig.getInjector().getInstance(PlatinoBDOrganizacionServiceImpl.class);
////				String uriPersona;
////				DatosBasicosPersonaItem datosPersona = null;
////				try {
////					uriPersona = platinoDBOrgPort.recuperarURIPersona(AgenteController.getAgente().usuarioldap);
////					datosPersona = platinoDBOrgPort.recuperarDatosPersona(uriPersona);
////					play.Logger.info(datosPersona.getCodigoUnidadOrg());
////					UnidadOrganicaCriteriaItem campos = new UnidadOrganicaCriteriaItem();
////					campos.setCodigoUnidadOrg(datosPersona.getCodigoUnidadOrg());
////					List<UnidadOrganicaItem> lstuo = platinoDBOrgPort.buscarUnidadesPorCampos(campos);
////					play.Logger.info(lstuo.get(0).getDescripcionUnidadOrg());
////				} catch (DBOrganizacionException_Exception e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
//			
//				if (respuesta != null && respuesta.error != null && respuesta.error.descripcion == null) {
//					dbComunicacionInterna.respuesta = respuesta;
//					dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.enviada.name();
//				}
//			} catch (ComunicacionesInternasServiceException e) {
//				log.error("No se ha podido crear el alta de la comunicación interna: "+e.getMessage());
//				Messages.error("No se ha podido crear el alta de la comunicación interna");
//			}
//		}
//		
//		Agente logAgente = AgenteController.getAgente();
//		if (!Messages.hasErrors()) {
//			dbComunicacionInterna.save();
//			log.info("Acción Editar de página: " + "gen/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html" + " , intentada con éxito " + " Agente: " + logAgente);
//		} else
//			log.info("Acción Editar de página: " + "gen/PaginaAltaComunicacionInterna/PaginaAltaComunicacionInterna.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
//		PaginaAltaComunicacionInternaController.frmNuevoAsientoRender(idSolicitud, idComunicacionInterna);
//	}
	
//	@Util
//	public static void frmNuevoAsientoRender(Long idSolicitud, Long idComunicacionInterna) {
//		if (!Messages.hasErrors()) {
//			Messages.ok("Página editada correctamente");
//			Messages.keep();
//			redirect("ComunicacionesInternasController.index", ComunicacionesInternasController.getAccion(), idSolicitud, idComunicacionInterna);
//		}
//		Messages.keep();
//		redirect("PaginaAltaComunicacionInternaController.index", "editar", idSolicitud, idComunicacionInterna);
//	}
	
	public static void seleccionar(Long id, List<Long> idsSeleccionados) {
		
	}
	
}
