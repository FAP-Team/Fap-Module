package services.platino;

import java.net.URL;

import javax.inject.Inject;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import platino.PlatinoProxy;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import utils.WSUtils;
import es.gobcan.platino.servicios.sgrde.SGRDEServicePortType;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;
import es.gobcan.platino.servicios.tramitacion.SoporteTramitacionService;
import es.gobcan.platino.servicios.tramitacion.SoporteTramitacionServiceBean;

public class PlatinoSoporteTramitacionServiceImpl {

	 private static Logger log = Logger.getLogger(PlatinoGestorDocumentalService.class);

	    private PropertyPlaceholder propertyPlaceholder;
	    private SoporteTramitacionServiceBean soportePort;

	    @Inject
	    public PlatinoSoporteTramitacionServiceImpl(PropertyPlaceholder propertyPlaceholder) {
	        this.propertyPlaceholder = propertyPlaceholder;

	        URL wsdl = PlatinoSoporteTramitacionServiceImpl.class.getClassLoader().getResource("wsdl/tramite.wsdl");
	        //Comprobar que está bien
	        soportePort = new SoporteTramitacionService(wsdl).getSoporteTramitacionService(); 
	        
	        WSUtils.configureEndPoint(soportePort, getEndPoint());
	        WSUtils.configureSecurityHeaders(soportePort, propertyPlaceholder);
	        PlatinoProxy.setProxy(soportePort, propertyPlaceholder);
	        
	        Client client = ClientProxy.getClient(soportePort);
			HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
			httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
			httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
			httpConduit.setClient(httpClientPolicy);			
	    }
	
	    private String getEndPoint() {
	        return FapProperties.get("fap.platino.soporteTramitacion.url");
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
	    
	    private String getVersion() {
	        return soportePort.getVersion();
	    }
		
		public boolean isConfigured() {
			if (hasConnection())
				return true;
			return false;
		}
		
		public void mostrarInfoInyeccion() {
			if (isConfigured())
				play.Logger.info("El servicio de Soporte a la Tramitacion ha sido inyectado con Platino y está operativo.");
			else
				play.Logger.info("El servicio de Soporte a la Tramitacion ha sido inyectado con Platino y NO está operativo.");
	    }
}
