package services.platino;

import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import messages.Messages;
import models.Solicitante;

import net.java.dev.jaxb.array.StringArray;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import es.gobcan.platino.servicios.localizaciones.*;
import es.gobcan.platino.servicios.terceros.*;

import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import platino.KeystoreCallbackHandler;
import platino.PlatinoCXFSecurityHeaders;
import platino.PlatinoProxy;
import play.libs.Codec;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.TercerosServiceException;
import sun.security.pkcs.PKCS7;
import utils.BinaryResponse;
import utils.TercerosUtils;
import utils.WSUtils;
import utils.XMLGregorianCalendarConverter;

/**
 * TercerosServiceImpl
 */
@InjectSupport
public class PlatinoTercerosServiceImpl implements services.TercerosService {

	private static Logger log = Logger.getLogger(PlatinoTercerosServiceImpl.class);
	
	private PlatinoLocalizacionesService localizacionesPort;
	
	private TercerosServiceBean tercerosPort;
	private PropertyPlaceholder propertyPlaceholder;
	
	@Inject
	public PlatinoTercerosServiceImpl(PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
        URL wsdlURL = PlatinoTercerosServiceImpl.class.getClassLoader().getResource("wsdl/terceros.wsdl");
        tercerosPort = new TercerosService(wsdlURL).getTercerosService();
        WSUtils.configureEndPoint(tercerosPort, getEndPoint());
        WSUtils.configureSecurityHeaders(tercerosPort, propertyPlaceholder);
        PlatinoProxy.setProxy(tercerosPort, propertyPlaceholder);
        
        Client client = ClientProxy.getClient(tercerosPort);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
		
		this.localizacionesPort = new PlatinoLocalizacionesService(propertyPlaceholder);
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Terceros ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de Terceros ha sido inyectado con Platino y NO está operativo.");
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

	private String getVersion() throws TercerosServiceException {
	    try {
	        String version = tercerosPort.getVersion();
	        return version;
	    }catch(Exception e){
	        throw newTercerosServiceException("Error al hacer getVersion: "+e.getMessage(), e);
	    }
	}
	
	protected TercerosServiceBean getTercerosPort(){
    	return this.tercerosPort;
    }
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.terceros.url");
	}
	
	private TercerosServiceException newTercerosServiceException(String msg, Exception cause){
	    return new TercerosServiceException(msg, cause);
	}
	
	public List<String> buscarTercero(String query) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTerceros(query);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Tercero con query: "+query+" - "+e.getMessage(), e);
		}
	}

	public List<String> buscarTerceroByItem(TerceroMinimalItem tmi) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosByItem(tmi);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Tercero con tmi: "+tmi.getUri()+" - "+e.getMessage(), e);
		}
	}	
	
	public TerceroItem consultarTercero(String uri) throws TercerosServiceException{
		try {
			return tercerosPort.consultarTercero(uri);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Tercero con tmi: "+uri+" - "+e.getMessage(), e);
		}
	}	
	
	public TerceroItem consultarTercero(TerceroListItem tercero) throws TercerosServiceException{
		return consultarTercero(tercero.getUri());
	}	
	
	public TerceroItem consultarTercero(TerceroMinimalItem tercero) throws TercerosServiceException{
		return consultarTercero(tercero.getUri());
	}	
	
	public List<TerceroListItem> buscarTercerosDetallados(String query) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosDetallados(query);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Terceros Detallados con query: "+query+" - "+e.getMessage(), e);
		}
	}

	public List<TerceroListItem> buscarTercerosDetalladosByItem(TerceroMinimalItem tmi) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosDetalladosByItem(tmi);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Terceros Detallados por tmi: "+tmi.getUri()+" - "+e.getMessage(), e);
		}
	}

	public List<TerceroListItem> buscarTercerosDetalladosByItem(Solicitante solicitante) throws TercerosServiceException{
		return buscarTercerosDetalladosByItem(TercerosUtils.convertirSolicitanteATerceroMinimal(solicitante));
	}
	
	public List<TerceroListItem> buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException{
		TerceroMinimalItem tercero = new TerceroMinimalItem();
		tercero.setNumeroDocumento(numeroIdentificacion);
		tercero.setTipoDocumento(TercerosUtils.convertirTipoNipATipoDocumentoItem(tipoIdentificacion));
		return buscarTercerosDetalladosByItem(tercero);
	}

	public PageDataUriItem buscarTercerosPaginados(String query) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosPaginados(query, 0, 4);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Terceros Paginados con query: "+query+" - "+e.getMessage(), e);
		}
	}

	public ProvinciaItem recuperarProvincia(Long idProvincia) {
		if (idProvincia == null)
			return null;
		try {
			return localizacionesPort.recuperarProvincia(idProvincia);
		} catch (Exception e) {
			return null;
		}
	}

	public PaisItem recuperarPais(Long idPais) {
		if (idPais == null)
			return null;
		try {
			return localizacionesPort.recuperarPais(idPais);
		} catch (Exception e) {
			return null;
		}
	}	

	public MunicipioItem recuperarMunicipio(Long idProvincia, Long idMunicipio) {
		if ((idMunicipio == null) || (idProvincia == null))
			return null;
		try {
			return localizacionesPort.recuperarMunicipio(idMunicipio, idProvincia);
		} catch (Exception e) {
			return null;
		}
	}	
	
	public IslaItem recuperarIsla(Long idIsla) {
		if (idIsla == null)
			return null;
		try {
			return localizacionesPort.recuperarIsla(idIsla);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String crearTerceroMinimal(TerceroMinimalItem tercero) throws TercerosServiceException{
		try {
			return tercerosPort.crearTerceroMinimal(tercero);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al intentar crear un Tercero en Platino: "+tercero.getNumeroDocumento()+" - "+e.getMessage(), e);
		}
	}
	
	public String crearTerceroMinimal(Solicitante tercero) throws TercerosServiceException{
		return crearTerceroMinimal(TercerosUtils.convertirSolicitanteATerceroMinimal(tercero));
	}

}
