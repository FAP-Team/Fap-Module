package services.platino;

import java.net.URL;

import javax.inject.Inject;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import es.gobcan.platino.servicios.edmyce.mensajes.*;
import es.gobcan.platino.servicios.registro.Registro_Service;
import services.MensajeServiceException;
import utils.WSUtils;

@InjectSupport
public class PlatinoMensajeServiceImpl implements services.MensajeService {

	private PropertyPlaceholder propertyPlaceholder;
	
	private MensajePortType mensajePort;
	
	@Inject
	public PlatinoMensajeServiceImpl(PropertyPlaceholder propertyPlaceholder) {
		
        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoGestorDocumentalService.class.getClassLoader().getResource("wsdl/mensaje.wsdl");
        mensajePort = new MensajeService(wsdlURL).getMensajeService();

        WSUtils.configureEndPoint(mensajePort, getEndPoint());
        WSUtils.configureSecurityHeaders(mensajePort, propertyPlaceholder);
        PlatinoProxy.setProxy(mensajePort, propertyPlaceholder);
        
        Client client = ClientProxy.getClient(mensajePort);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
    }
    

	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Mensajes ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de Mensajes ha sido inyectado con Platino y NO está operativo.");
    }
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection =  getVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
    private String getVersion() {
        return mensajePort.getVersion();
    }
    
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.mensajes.url");
	}
	
}
