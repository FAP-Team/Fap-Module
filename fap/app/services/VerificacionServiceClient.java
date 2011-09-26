package services;

import java.net.URL;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

import models.SolicitudGenerica;

import org.apache.log4j.Logger;


import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.verificacion.ws.VerificacionWebServiceInterface;
import es.gobcan.eadmon.verificacion.ws.servicios.IniciarVerificacionRequest;
import es.gobcan.eadmon.verificacion.ws.servicios.IniciarVerificacionResponse;

import platino.FirmaClient;
import platino.PlatinoProxy;
import play.libs.WS;

public class VerificacionServiceClient {

	private static Logger log = Logger.getLogger(VerificacionServiceClient.class);

	private static VerificacionWebServiceInterface verificacionService;

	
	static {
		
//		URL wsdlURL = Aed.class.getClassLoader().getResource ("aed/aed.wsdl");
//		aed = new Aed(wsdlURL).getAed(new MTOMFeature());
//		
//		BindingProvider bp = (BindingProvider) aed;
//		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, Fap.getConfiguration().get("fap.aed.url"));		
//	
//		PlatinoProxy.setProxy(aed);
	}
	
	/**
	 * Inicia la verificacion para una solicitud
	 * @param solicitud Solicitud
	 * @return URI de la verificación
	 * @throws VerificacionWSExcepcion 
	 */
	public static String iniciarVerificacion(SolicitudGenerica solicitud) {//throws VerificacionWSExcepcion{
		IniciarVerificacionRequest request = new IniciarVerificacionRequest();
//		request.setUriProcedimiento(PROCEDIMIENTO);
//		request.setUriTramite(TRAMITE);
//		request.setExpediente(solicitud.expediente.idAED);
//		//TODO: Poner id correspondiente 
////		request.setExpediente("IDT20100001");
//		
//		request.setMultiples(null);
//		request.setEspecificacionTipoBase(listaEspecificacionesTipoBase(solicitud));
//		request.setEspecificacionObligatoriedad(listaEspecificacionObligatoriedad(solicitud));
//		
//		
//		IniciarVerificacionResponse response = verificacionService.iniciarVerificacion(request);
//		return response.getUriVerificacion();
		return "prueba";
	}
}
