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
import utils.WSUtils;

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
	        throw newTercerosServiceException("Error al hacer getVersion", e);
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
			throw newTercerosServiceException("Fallo al buscar el Tercero con query: "+query, e);
		}
	}

	public List<String> buscarTerceroByItem(TerceroMinimalItem tmi) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosByItem(tmi);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Tercero con tmi: "+tmi.getUri(), e);
		}
	}	

	public List<TerceroListItem> buscarTercerosDetallados(String query) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosDetallados(query);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Terceros Detallados con query: "+query, e);
		}
	}

	public List<TerceroListItem> buscarTercerosDetalladosByItem(TerceroMinimalItem tmi) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosDetalladosByItem(tmi);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Terceros Detallados por tmi: "+tmi.getUri(), e);
		}
	}


	public PageDataUriItem buscarTercerosPaginados(String query) throws TercerosServiceException{
		try {
			return tercerosPort.buscarTercerosPaginados(query, 0, 4);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al buscar el Terceros Paginados con query: "+query, e);
		}
	}

	public TerceroItem consultarTercero(String uri) throws TercerosServiceException{
		try {
			return tercerosPort.consultarTercero(uri);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al consultar Tercero con uri: "+uri, e);
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
			return localizacionesPort.recuperarMunicipio(idProvincia, idMunicipio);
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

	/**
	 * Método para mapear los datos de tercero del objeto TerceroItem a la clase Solicitante de FAP
	 * @param tercero 
	 * @return Solicitante Devuelve un objeto solicitante(FAP)
	 */

	public Solicitante convertirTerceroASolicitante(TerceroItem tercero) throws TercerosServiceException{

		Solicitante  s= null;
		if(tercero!=null){
			s = new Solicitante();

			if(tercero.getTipoTercero().getId().equalsIgnoreCase("JURIDICO")||
					tercero.getTipoTercero().getDescripcion().equalsIgnoreCase("ORGANISMO")){
				s.tipo="JURIDICO";
				s.juridica.cif = tercero.getNumeroDocumento();
				s.juridica.entidad = tercero.getNombre();
				if(tercero.getEmails()!=null &&tercero.getEmails().size()>0)
				{	

					EmailItem correo = buscarCorreoPrincipal(tercero.getEmails());
					if(correo!=null){
						s.juridica.email = correo.getDireccion();
					}
				}

			}
			if(tercero.getTipoTercero().getId().equalsIgnoreCase("FISICO")){
				s.tipo="FISICO";
				if(tercero.getTipoDocumento()!=null)
					s.fisica.nip.tipo = tercero.getTipoDocumento().getId();
				s.fisica.nip.valor=tercero.getNumeroDocumento();
				s.fisica.nombre = tercero.getNombre();
				s.fisica.primerApellido = tercero.getApellido1();
				s.fisica.segundoApellido = tercero.getApellido2();
			}
			if(tercero.getEmails()!=null &&tercero.getEmails().size()>0)
			{	
				EmailItem correo = buscarCorreoPrincipal(tercero.getEmails());
				if(correo!=null)
					s.email = correo.getDireccion();
			}

			//EXISTEN LA POSIBILIDAD DE RECIBIR HASTA TRES DOMICILIOS DE LA BASE DE DATOS DE TERCERO SEGÚN
			//EL MODELO DE GESTIÓN DE LA BASE DE DATOS DE TERCEROS. SE ASOCIARÁ SOLAMENTE EL MARCADO COMO PRINCIPAL.
			//En cualquier caso FAP solo admite uno por tipo en el objeto de la clase
			//solicitante
			if(tercero.getDomicilios()!=null && tercero.getDomicilios().size()>0){
				for(int i=0;i<tercero.getDomicilios().size();i++){
					DomicilioItem d = tercero.getDomicilios().get(0);
					if(d.isPrincipal()){
						s.domicilio.calle=d.getVia();
						s.domicilio.codigoPostal=d.getCodigoPostal();
						MunicipioItem mun = recuperarMunicipio(d.getIdProvincia(), d.getIdMunicipio());
						if(mun!=null)
							s.domicilio.municipio= mun.getLiteral();
						s.domicilio.numero=d.getPortal();
						s.domicilio.otros=d.getOtros();
						PaisItem pais = recuperarPais(d.getIdPais());
						if(pais!=null)
							s.domicilio.pais=pais.getLiteral();
						ProvinciaItem prov = recuperarProvincia(d.getIdProvincia());
						if(prov!=null)
							s.domicilio.provincia= prov.getLiteral();
						s.domicilio.provinciaInternacional=d.getEstado();
						IslaItem isla = recuperarIsla(d.getIdIsla());
						if (isla!=null)
							s.domicilio.isla=isla.getLiteral();
					}
				}
			}

			//Existe posibilidad de recibir varios número de teléfono si tener claro cuantos de cada tipo puede almacenarse
			//en la base de datos de tercero. En cualquier caso FAP solo admite uno por tipo en el objeto de la clase
			//solicitante
			if(tercero.getTelefonos()!=null && tercero.getTelefonos().size()>0){
				TelefonoItem tfno = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"FIJO");
				if(tfno!=null)
					s.telefonoFijo = tfno.getNumero();

				TelefonoItem tfnoMovil = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"MOVIL");
				if(tfnoMovil!=null)
					s.telefonoMovil = tfnoMovil.getNumero();

				TelefonoItem fax = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"FAX");
				if(fax!=null)
					s.fax = fax.getNumero();

			}
		}

		return s;
	}

	private EmailItem buscarCorreoPrincipal(List<EmailItem> emails) {
		EmailItem correo = null;
		if(emails!=null){
			for(int i=0;i<emails.size();i++){
				if(emails.get(i).isPrincipal()){
					if((correo!=null && correo.getFechaActualizacion().compare(emails.get(i).getFechaActualizacion())== DatatypeConstants.LESSER)||
							(correo!=null && !correo.isPrincipal())){
						correo = emails.get(i);
					}else{
						if(correo==null){
							correo= emails.get(i);
						}
					}

				}else{
					if(correo==null)
						correo = emails.get(i);
				}
			}
		}
		return correo;
	}


	private TelefonoItem buscarTfnoPrincipalRegistrado (List<TelefonoItem> telefonos,String tipo) {
		TelefonoItem tfno = null;
		if(telefonos!=null){
			for(int i=0;i<telefonos.size();i++){

				if(telefonos.get(i).getTipo().getId().equalsIgnoreCase(tipo)&& telefonos.get(i).isPrincipal()){
					if((tfno!=null && tfno.getFechaActualizacion().compare(telefonos.get(i).getFechaActualizacion())== DatatypeConstants.LESSER)||
							(tfno!=null && !tfno.isPrincipal())){
						tfno= telefonos.get(i);
					}else{
						if(tfno==null){
							tfno= telefonos.get(i);
						}
					}
				}
				else{
					if(tfno==null && telefonos.get(i).getTipo().getId().equalsIgnoreCase(tipo))
						tfno = telefonos.get(i);
				}
			}
		}
		return tfno;
	}

}
