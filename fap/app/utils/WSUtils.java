package utils;

import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;

import messages.Messages;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;

import properties.FapProperties;

public class WSUtils {

	private static Logger logger = Logger.getLogger(WSUtils.class);
	
	public static void configureEndPoint(Object service, String endPoint){
		BindingProvider bpService = (BindingProvider) service;
		bpService.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
	}
	
	public static void aedError(String error, ProcedimientosExcepcion e){
		aedError(error, e.getFaultInfo().getDescripcion());
	}
	
	public static void aedError(String error, TiposDocumentosExcepcion e){
		aedError(error, e.getFaultInfo().getDescripcion());
	}
	
	private static void aedError(String error, String descripcion){
		logger.error(error + " : " + descripcion);
		Messages.error(error);		
	}
}
