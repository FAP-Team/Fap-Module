package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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
import services.BDOrganizacionService;
import services.ServiciosGenericosService;
import services.platino.PlatinoBDOrganizacionServiceImpl;
import tags.ComboItem;
import utils.ComunicacionesInternasUtils;
import utils.ServiciosGenericosUtils;
import validation.CustomValidation;

import com.google.gson.Gson;
import com.google.inject.spi.Message;

import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PaginaDatosNuevaComunicacionInternaControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;

public class PaginaDatosNuevaComunicacionInternaController extends PaginaDatosNuevaComunicacionInternaControllerGen {
	
	@Inject
	static BDOrganizacionService platinoDBOrgPort;
	
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
			
			if (comunicacionInterna.asientoAmpliado == null)
				comunicacionInterna.asientoAmpliado = new AsientoAmpliadoCIFap();
			
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

		CustomValidation.valid("comunicacionInterna.asientoAmpliado", comunicacionInterna.asientoAmpliado);
		CustomValidation.valid("comunicacionInterna", comunicacionInterna);
		
		if (dbComunicacionInterna.asientoAmpliado == null)
			dbComunicacionInterna.asientoAmpliado = new AsientoAmpliadoCIFap();
		
		dbComunicacionInterna.asientoAmpliado.interesado = comunicacionInterna.asientoAmpliado.interesado;
		CustomValidation.valid("comunicacionInterna.asientoAmpliado.unidadOrganicaOrigen", comunicacionInterna.asientoAmpliado.unidadOrganicaOrigen);
		CustomValidation.required("comunicacionInterna.asientoAmpliado.unidadOrganicaOrigen.codigo", comunicacionInterna.asientoAmpliado.unidadOrganicaOrigen.codigo);
		Long codigoUOOrigen = comunicacionInterna.asientoAmpliado.unidadOrganicaOrigen.codigo;
		if (codigoUOOrigen != null) {
			dbComunicacionInterna.asientoAmpliado.unidadOrganicaOrigen = getUnidadOrganicaFAP(codigoUOOrigen);
			dbComunicacionInterna.asientoAmpliado.unidadOrganicaOrigen.codigo = codigoUOOrigen;
		}
		CustomValidation.valid("comunicacionInterna.asientoAmpliado.unidadOrganicaDestino", comunicacionInterna.asientoAmpliado.unidadOrganicaDestino);
		CustomValidation.required("comunicacionInterna.asientoAmpliado.unidadOrganicaDestino.codigo", comunicacionInterna.asientoAmpliado.unidadOrganicaDestino.codigo);
		Long codigoUODestino = comunicacionInterna.asientoAmpliado.unidadOrganicaDestino.codigo;
		if (codigoUODestino != null) {
			dbComunicacionInterna.asientoAmpliado.unidadOrganicaDestino = getUnidadOrganicaFAP(codigoUODestino);
			dbComunicacionInterna.asientoAmpliado.unidadOrganicaDestino.codigo = codigoUODestino;
		}
		CustomValidation.required("comunicacionInterna.asientoAmpliado.resumen", comunicacionInterna.asientoAmpliado.resumen);
		dbComunicacionInterna.asientoAmpliado.resumen = comunicacionInterna.asientoAmpliado.resumen;
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
						lstCombo.add(new ComboItem(unidad.codigo, unidad.codigoCompleto + " - " + unidad.descripcion));
						
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

	public static List<ComboItem> uoOrigen(){
		List<ComboItem> lstCombo = new ArrayList<ComboItem>();
		try {
			Agente agente = AgenteController.getAgente();
			String uri = platinoDBOrgPort.recuperarURIPersona(agente.usuarioldap);		
			
			Date today = new Date();
			GregorianCalendar gregory = new GregorianCalendar();
			gregory.setTime(today);
			XMLGregorianCalendar fecha = null;
			fecha = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);

			List<UnidadOrganicaItem> lstUO = platinoDBOrgPort.consultarPertenenciaUnidad(uri, fecha);			
			for (UnidadOrganicaItem uo: lstUO){
				ReturnUnidadOrganicaFap unidad = null;
				if ((uo != null) && (uo.getCodigoUnidadOrg() != null))
					unidad = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigoBDOrganizacion = ?", uo.getCodigoUnidadOrg()).first();
				
				if (unidad != null)
					lstCombo.add(new ComboItem(unidad.codigo, unidad.codigoCompleto + " - " + unidad.descripcion));
				else
					play.Logger.error("No se tiene equivalencia en HiperReg para la unidad orgánica: (codigo: " + uo.getCodigoUnidadOrg() + ", descripcion: " + uo.getDescripcionUnidadOrg() + ")");
			}
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.error("No se pueden obtener la unidades orgánicas de origen del solicitante: " + e.getMessage());
			Messages.error("No se pueden obtener las unidades orgánicas de origen del solicitante");
		} catch (DatatypeConfigurationException e) {
			play.Logger.error("Error calculando la fecha para obtener las unidades orgánicas del solicitante: " + e.getMessage());
			Messages.error("No se pueden obtener las unidades orgánicas de origen del solicitante");
	    } 
		
		Messages.keep();
		return lstCombo;
	}
	
}
