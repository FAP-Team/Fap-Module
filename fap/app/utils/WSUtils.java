package utils;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import config.InjectorConfig;

import messages.Messages;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;

import platino.KeystoreCallbackHandler;
import platino.PlatinoCXFSecurityHeaders;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.platino.PlatinoBDOrganizacionServiceImpl;

public class WSUtils {

	private static Logger logger = Logger.getLogger(WSUtils.class);
	
	public static void configureEndPoint(Object service, String endPoint){
		BindingProvider bpService = (BindingProvider) service;
		bpService.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
	}
	
	public static void configureSecurityHeaders(Object service, PropertyPlaceholder propertyPlaceholder){
		String backoffice = propertyPlaceholder.get("fap.platino.security.backoffice.uri");
		String certificadoalias = propertyPlaceholder.get("fap.platino.security.certificado.alias");
		
		PlatinoCXFSecurityHeaders.addSoapWSSHeader(
				service,
				PlatinoCXFSecurityHeaders.SOAP_11,
				backoffice,
				certificadoalias,
				KeystoreCallbackHandler.class.getName(),
				null);
	}
	
	public static void configureSecurityHeadersWithUser(Object service, String userUri){
		PlatinoCXFSecurityHeaders.changeUsernameToken(service, userUri);		
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
	
	public static void configureDebug(Object service){
		Client client = ClientProxy.getClient(service);
		client.getInInterceptors().add(new LoggingInInterceptor());
		client.getOutInterceptors().add(new LoggingOutInterceptor());
		
		HTTPConduit http = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(36000);
		httpClientPolicy.setAllowChunking(false);
		httpClientPolicy.setContentType("text/xml; charset=ISO-8859-1;");
		http.setClient(httpClientPolicy);
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 * @throws RuntimeException
	 */
	public static XMLGregorianCalendar getXmlGregorianCalendar(Date date) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		XMLGregorianCalendar result;
		try {
		    result = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
		}catch(DatatypeConfigurationException e){
		    throw new RuntimeException(e);
		}
		return result;
	}
	
	public static XMLGregorianCalendar getXmlGregorianCalendar(DateTime date) {
	    return getXmlGregorianCalendar(date.toDate()); 
	}
	
	public static void restoreSecurityHeadersBackoffice(Object service) {
		String backoffice = FapProperties.get("fap.platino.security.backoffice.uri");
		configureSecurityHeadersWithUser(service, backoffice);
	}
	
	public static void setupSecurityHeadersWithUser(Object service, String uid) {
		try {
			PlatinoBDOrganizacionServiceImpl platinoDBOrgPort = InjectorConfig.getInjector().getInstance(PlatinoBDOrganizacionServiceImpl.class);
			String userUri = platinoDBOrgPort.recuperarURIPersona(uid);
			configureSecurityHeadersWithUser(service, userUri);
			
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al configurar cabecera de seguridad para el usuario: " + uid + ". " + e.getMessage());
		}
	}
}
