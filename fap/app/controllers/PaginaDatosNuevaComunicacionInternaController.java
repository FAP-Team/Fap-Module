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
import models.AsientoCIFap;
import models.ComunicacionInterna;
import models.Documento;
import models.ListaUris;
import models.ReturnUnidadOrganicaFap;
import models.SolicitudGenerica;
import play.mvc.Util;
import properties.FapProperties;
import services.genericos.ServiciosGenericosUtils;
import tags.ComboItem;
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
	
	@Util
	public static void PaginaDatosNuevaComunicacionInternaValidateCopy(String accion, ComunicacionInterna dbComunicacionInterna, ComunicacionInterna comunicacionInterna) {
		CustomValidation.clearValidadas();

		CustomValidation.valid("comunicacionInterna.asiento", comunicacionInterna.asiento);
		CustomValidation.valid("comunicacionInterna", comunicacionInterna);
		
		if (dbComunicacionInterna.asiento == null)
			dbComunicacionInterna.asiento = new AsientoCIFap();
		
		dbComunicacionInterna.asiento.interesado = comunicacionInterna.asiento.interesado;
		CustomValidation.required("comunicacionInterna.asiento.resumen", comunicacionInterna.asiento.resumen);
		dbComunicacionInterna.asiento.resumen = comunicacionInterna.asiento.resumen;
		
		dbComunicacionInterna.asiento.asientoAmpliado = comunicacionInterna.asiento.asientoAmpliado;
		
		if (dbComunicacionInterna.asiento.asientoAmpliado) {
			CustomValidation.valid("comunicacionInterna.asiento.unidadOrganicaOrigen", comunicacionInterna.asiento.unidadOrganicaOrigen);
			CustomValidation.required("comunicacionInterna.asiento.unidadOrganicaOrigen.codigo", comunicacionInterna.asiento.unidadOrganicaOrigen.codigo);
			Long uoOrigencodigoUO = comunicacionInterna.asiento.unidadOrganicaOrigen.codigo;
			if (uoOrigencodigoUO != null) {
				dbComunicacionInterna.asiento.unidadOrganicaOrigen = ServiciosGenericosUtils.getUnidadOrganicaFAP(uoOrigencodigoUO);
				dbComunicacionInterna.asiento.unidadOrganicaOrigen.codigo = uoOrigencodigoUO;
			}
		} else
			dbComunicacionInterna.asiento.unidadOrganicaOrigen = null;

		
		CustomValidation.valid("comunicacionInterna.asiento.unidadOrganicaDestino", comunicacionInterna.asiento.unidadOrganicaDestino);
		CustomValidation.required("comunicacionInterna.asiento.unidadOrganicaDestino.codigo", comunicacionInterna.asiento.unidadOrganicaDestino.codigo);
		Long uoDestinocodigoUO = comunicacionInterna.asiento.unidadOrganicaDestino.codigo;
		if (uoDestinocodigoUO != null) {
			dbComunicacionInterna.asiento.unidadOrganicaDestino = ServiciosGenericosUtils.getUnidadOrganicaFAP(uoDestinocodigoUO);
			dbComunicacionInterna.asiento.unidadOrganicaDestino.codigo = uoDestinocodigoUO;
		}
		
		Agente agente = AgenteController.getAgente();
		if (agente != null && agente.usuarioldap != null && !agente.usuarioldap.isEmpty())
			dbComunicacionInterna.asiento.userId = agente.usuarioldap;
		
		dbComunicacionInterna.estado = EstadosComunicacionInternaEnum.creada.name();
	}
	
}
