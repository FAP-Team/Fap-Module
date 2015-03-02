package services.platino;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import es.gobcan.platino.servicios.organizacion.*;
import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.BDOrganizacionService;
import utils.WSUtils;

@InjectSupport
public class PlatinoBDOrganizacionServiceImpl implements BDOrganizacionService {
	
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
	
	@Override
	public boolean isConfigured() {
		return hasConnection();
	}

	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de BDOrganización ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de BDOrganización ha sido inyectado con Platino y NO está operativo.");
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
			play.Logger.info("Error al obtener la versión del servicio DBOrganización");
	        throw new DBOrganizacionException_Exception("Error al hacer getVersion del servicio DBOrganización: "+e.getMessage(), e);
	    }
	}
	
	/**
	 * Método que recupera la uri de una persona de la base de datos de organización mediante el nombre de usuario.
	 * @param uid
	 * @return
	 * @throws DBOrganizacionException_Exception
	 */
	@Override
	public String recuperarURIPersona(String uid) throws DBOrganizacionException_Exception {
		List<String> lstURIPersona = null;
		
		try {
			if (uid != null)
				lstURIPersona = dbOrgPort.recuperarURIPersona(uid, null, true);
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar el usuario desde BD Orgaizacion de Platino");
			throw new DBOrganizacionException_Exception("Error al hacer recuperarUiPersona: "+ e.getMessage(), e);
		} catch (Exception e) {
			play.Logger.info("Error al recuperar el usuario desde BD Orgaizacion de Platino");
		}
		
		if ((lstURIPersona != null) && (!lstURIPersona.isEmpty()))
			return lstURIPersona.get(0);
		else
			return null;
	}
	
	/**
	 * Método que recupera la información de una persona mediante la uri recuperada de la base de datos de organización.
	 * @param uri
	 * @return
	 * @throws DBOrganizacionException_Exception
	 */
	@Override
	public DatosBasicosPersonaItem recuperarDatosPersona(String uri) throws DBOrganizacionException_Exception {
		List<DatosBasicosPersonaItem> lstdatosPersona = null;
		
		try{
			if (uri != null && !uri.isEmpty())
				lstdatosPersona = dbOrgPort.recuperarDatosPersona(uri);
		}catch(Exception e){
			play.Logger.info("Error al recuperar los datos de la persona");
			throw new DBOrganizacionException_Exception("Error al hacer recuperarDatosPersona: "+ e.getMessage(), e);
		}
		
		if ((lstdatosPersona != null) && (!lstdatosPersona.isEmpty()))
			return lstdatosPersona.get(0);
		else
			return null;
	}
	
	/**
	 * Método que permite hacer una búsqueda de las unidades orgánicas de la base de datos de organización por medio de una 
	 * serie de campos.
	 * @param campos
	 * @return
	 * @throws DBOrganizacionException_Exception
	 */
	@Override
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
	
	/**
	 * Método que permite realizar la búsqueda de unidades orgánicas de la base de datos de organización mediante una 
	 * consulta a la misma.
	 * @param consulta
	 * @return
	 * @throws DBOrganizacionException_Exception
	 */
	@Override
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
	
	/**
	 * Método que recupera las unidades orgánicas a las que pertenece un determinado individuo de la base de datos de organicación.
	 * @param uriFuncionario
	 * @param fecha
	 * @return
	 * @throws DBOrganizacionException_Exception
	 */
	@Override
	public List<UnidadOrganicaItem> consultarPertenenciaUnidad(String uriFuncionario, XMLGregorianCalendar fecha) throws DBOrganizacionException_Exception{
        List<UnidadOrganicaItem> unidadesOrganicas = null;
		
		try {
			unidadesOrganicas = dbOrgPort.consultarPertenenciaUnidad(uriFuncionario, fecha);
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar las unidades orgánicas desde BD Orgaizacion de Platino");
			throw new DBOrganizacionException_Exception("Error al hacer consultarPertenenciaUnidad: "+ e.getMessage(), e);
		}
		return unidadesOrganicas;
	}
	
	/**
	 * Método que recupera los datos detalladados de una determinada unidad orgánica de la base de datos de organicación.
	 * @param uri
	 * @param fecha
	 * @return
	 * @throws DBOrganizacionException_Exception
	 */
	@Override
	public UnidadOrganicaItem consultaDetalladaDeUnidad(String uriUO, XMLGregorianCalendar fecha) throws DBOrganizacionException_Exception{
		UnidadOrganicaItem unidadesOrganicaDetallada = null;
			
		try {
			unidadesOrganicaDetallada = dbOrgPort.consultaDetalladaDeUnidad(uriUO, fecha);
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar los datos detalladados de la unidades orgánica desde BD Orgaizacion de Platino");
			throw new DBOrganizacionException_Exception("Error al hacer consultaDetalladaDeUnidad: "+ e.getMessage(), e);
		}
		return unidadesOrganicaDetallada;
	}
	
	/**
	 * Método que recupera las uris del personal inscrito en una determinada unidad orgánica de la base de datos de organicación.
	 * @param uriUO
	 * @param fecha
	 * @return
	 * @throws DBOrganizacionException_Exception
	 */
	@Override
	public List<String> consultarPersonalAdscritoAUnidad(String uriUO, XMLGregorianCalendar fecha) throws DBOrganizacionException_Exception{
		List<String> uriFuncionarios = null;
		
		try {
			 uriFuncionarios = dbOrgPort.consultarPersonalAdscritoAUnidad(uriUO, fecha);
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al recuperar las uris de los funcionarios adscritos a la unidades orgánica desde BD Orgaizacion de Platino");
			throw new DBOrganizacionException_Exception("Error al hacer consultarPersonalAdscritoAUnidad: "+ e.getMessage(), e);
		}
		return uriFuncionarios;
	}
}
