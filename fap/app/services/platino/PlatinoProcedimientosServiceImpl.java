package services.platino;

import java.net.URL;

import javax.inject.Inject;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import config.InjectorConfig;

import es.gobcan.platino.servicios.procedimientos.DBProcedimientosException_Exception;
import es.gobcan.platino.servicios.procedimientos.DBProcedimientosServiceBean;
import es.gobcan.platino.servicios.procedimientos.DBProcedimientosServiceBeanService;
import es.gobcan.platino.servicios.procedimientos.TipoDocumentoWSItem;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;
import es.gobcan.platino.servicios.terceros.TercerosServiceBean;
import es.gobcan.platino.servicios.terceros.TercerosServiceBeanService;

import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.TercerosServiceException;
import utils.WSUtils;

@InjectSupport
public class PlatinoProcedimientosServiceImpl {
	private static Logger log = Logger.getLogger(PlatinoProcedimientosServiceImpl.class);
	
	private final DBProcedimientosServiceBean procedimientosPort;	  
		
	private final PropertyPlaceholder propertyPlaceholder;

	@Inject
    public PlatinoProcedimientosServiceImpl(PropertyPlaceholder propertyPlaceholder) {
	        this.propertyPlaceholder = propertyPlaceholder;

	        //Instanciacion del servicio de gestorDoc
	        URL wsdlURL = PlatinoProcedimientosServiceImpl.class.getClassLoader().getResource("wsdl/dbprocedimientos.wsdl");
	        procedimientosPort = new DBProcedimientosServiceBeanService(wsdlURL).getDBProcedimientosServiceBeanPort();
	        
	        WSUtils.configureEndPoint(procedimientosPort, getEndPoint());
	        WSUtils.configureSecurityHeaders(procedimientosPort, propertyPlaceholder);
	        PlatinoProxy.setProxy(procedimientosPort, propertyPlaceholder);
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Procedimientos de Platino ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de Procedimientos de Platino ha sido inyectado con Platino y NO está operativo.");
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

	private String getVersion() throws ProcedimientosServiceException {
	    try {
	        String version = procedimientosPort.getVersion();
	        return version;
	    }catch(Exception e){
	        throw new ProcedimientosServiceException("Error al hacer getVersion: "+e.getMessage(), e);
	    }
	}
	
	protected DBProcedimientosServiceBean getProcedimientosPort(){
    	return this.procedimientosPort;
    }
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.procedimientos.url");
	}
	
	public boolean buscarProcedimientos (String nombre){
		try {
			System.out.println("Respuesta "+procedimientosPort.buscarProcedimientos(null, nombre, null));
			if (procedimientosPort.buscarProcedimientos(null, nombre, null) != null){
				return true;
			}
		} catch (DBProcedimientosException_Exception e) {
			play.Logger.error("El procedimiento "+nombre+" no existe");
		}
		return false;
	}
	
	public TipoDocumentoWSItem getTipoDocumento(String tipo) {
		try {
			return procedimientosPort.getTipoDocumento(tipo, null);
		} catch (DBProcedimientosException_Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
