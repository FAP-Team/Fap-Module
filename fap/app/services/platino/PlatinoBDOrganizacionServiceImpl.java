package services.platino;

import java.net.URL;

import javax.inject.Inject;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import es.gobcan.platino.servicios.organizacion.*;

import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import utils.WSUtils;

@InjectSupport
public class PlatinoBDOrganizacionServiceImpl {
	
	private final PropertyPlaceholder propertyPlaceholder;
	
	private final DBOrganizacionServiceBean dbOrgPort;
	
	@Inject
	public PlatinoBDOrganizacionServiceImpl(PropertyPlaceholder propertyPlaceholder) {
		this.propertyPlaceholder = propertyPlaceholder;
		
        URL wsdlURL = PlatinoBDOrganizacionServiceImpl.class.getClassLoader().getResource("wsdl/dborganizacion.wsdl");
        dbOrgPort = new DBOrganizacionService(wsdlURL).getDBOrganizacionService();
        
        WSUtils.configureEndPoint(dbOrgPort, getEndPoint());
        WSUtils.configureSecurityHeaders(dbOrgPort, propertyPlaceholder);
        PlatinoProxy.setProxy(dbOrgPort, propertyPlaceholder);
        
        Client client = ClientProxy.getClient(dbOrgPort);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
	}

	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.organizacion.url");
	}
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			
			hasConnection = getVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
			
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
	private String getVersion() throws DBOrganizacionException_Exception {
	    try {
	    	
	        String version = dbOrgPort.getVersion();
	        return version;
	        
	    }catch(Exception e){
			play.Logger.info("Error al hacer getVersion");
	        throw new DBOrganizacionException_Exception("Error al hacer getVersion: "+e.getMessage(), e);
	    }
	}
	
	public String recuperarURIPersona(String uid) throws DBOrganizacionException_Exception {
		try {
			
			return dbOrgPort.recuperarURIPersona(uid, null, true).get(0);
			
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar el usuario desde BD Orgaizacion de Platino");
			throw new DBOrganizacionException_Exception("Error al hacer recuperarUiPersona: "+ e.getMessage(), e);
		}
	}
}
