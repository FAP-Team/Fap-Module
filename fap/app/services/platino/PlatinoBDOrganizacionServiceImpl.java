package services.platino;

import java.net.URL;
import java.util.List;

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
	}
	
	public PlatinoBDOrganizacionServiceImpl(PropertyPlaceholder propertyPlaceholder, DBOrganizacionServiceBean dbOrgPortRecibido) {
		this.propertyPlaceholder = propertyPlaceholder;
		
		if (dbOrgPortRecibido != null) {
			 dbOrgPort = dbOrgPortRecibido;
		} else {
	        URL wsdlURL = PlatinoBDOrganizacionServiceImpl.class.getClassLoader().getResource("wsdl/dborganizacion.wsdl");
	        dbOrgPort = new DBOrganizacionService(wsdlURL).getDBOrganizacionService();
	        
	        WSUtils.configureEndPoint(dbOrgPort, getEndPoint());
	        WSUtils.configureSecurityHeaders(dbOrgPort, propertyPlaceholder);
	        PlatinoProxy.setProxy(dbOrgPort, propertyPlaceholder);
		}
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
			List<String> lstURIPersona = null;
			
			if (uid != null)
				lstURIPersona = dbOrgPort.recuperarURIPersona(uid, null, true);
			
			if ((lstURIPersona != null) && (!lstURIPersona.isEmpty()))
				return lstURIPersona.get(0);
			else
				return null;
			
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar el usuario desde BD Orgaizacion de Platino");
			throw new DBOrganizacionException_Exception("Error al hacer recuperarUiPersona: "+ e.getMessage(), e);
		}
	}
	
	public DatosBasicosPersonaItem recuperarDatosPersona(String uri) throws DBOrganizacionException_Exception {
		List<DatosBasicosPersonaItem> lstdatosPersona = null;
		
		try{
			if (uri != null && !uri.isEmpty())
				lstdatosPersona = dbOrgPort.recuperarDatosPersona(uri);
			
			if ((lstdatosPersona != null) && (!lstdatosPersona.isEmpty()))
				return lstdatosPersona.get(0);
			else
				return null;
		}catch(Exception e){
			play.Logger.info("Error al recuperar los datos de la persona");
			throw new DBOrganizacionException_Exception("Error al hacer recuperarDatosPersona: "+ e.getMessage(), e);
		}
	}
	
	public List<UnidadOrganicaItem> buscarUnidadesPorCampos(UnidadOrganicaCriteriaItem campos) throws DBOrganizacionException_Exception{
		List<UnidadOrganicaItem> unidadOrganicas = null;
		
		try {
			unidadOrganicas = dbOrgPort.buscarUnidadesPorCampos(campos);
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar la Unidad Orgánica en DBOrganización");
			throw new DBOrganizacionException_Exception("Error al hacer recuperarUiPersona: "+ e.getMessage(), e);
		}
		
		return unidadOrganicas;
	}
	
	public List<UnidadOrganicaItem> buscarUnidadesPorConsulta(String consulta) throws DBOrganizacionException_Exception{
		List<UnidadOrganicaItem> unidadesOrganicas = null;
		
		try {
			unidadesOrganicas = dbOrgPort.buscarUnidadesPorConsulta(consulta);
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar el usuario desde BD Orgaizacion de Platino");
			throw new DBOrganizacionException_Exception("Error al hacer recuperarUiPersona: "+ e.getMessage(), e);
		}
		return unidadesOrganicas;
	}
}
