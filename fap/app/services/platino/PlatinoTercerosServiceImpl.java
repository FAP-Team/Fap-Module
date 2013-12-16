package services.platino;

import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import models.Agente;
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

import config.InjectorConfig;

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
        tercerosPort = new TercerosServiceBeanService(wsdlURL).getTercerosServiceBeanPort();
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

	public List<TerceroListItem> buscarTercerosDetalladosByItem(TerceroMinimalItem tmi) throws TercerosServiceException{
		try {
			log.info("[buscarTercerosDetalladosByNumeroIdentificacion] Numero doc: "+tmi.getNumeroDocumento());
			log.info("[buscarTercerosDetalladosByNumeroIdentificacion] Tipo doc: "+tmi.getTipoDocumento());
			return tercerosPort.buscarTercerosDetalladosByItem(tmi);
		} catch (Exception e) {
			e.printStackTrace();
			throw newTercerosServiceException("Fallo al buscar el Terceros Detallados por tmi: "+tmi.getUri()+" - "+e.getMessage(), e);
		}
	}
	
	public Solicitante buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException{
		TerceroMinimalItem tercero = new TerceroMinimalItem();
		tercero.setNumeroDocumento(numeroIdentificacion);
		tercero.setTipoDocumento(convertirTipoNipATipoDocumentoItem(tipoIdentificacion));
	
		List<TerceroListItem> tercerosListItem = buscarTercerosDetalladosByItem(tercero);
		
		if ((tercerosListItem != null) && (!tercerosListItem.isEmpty())){
			Collections.sort(tercerosListItem, new ComparadorFechaTerceroListItem());
			TerceroItem terceroItem = consultarTercero(tercerosListItem.get(0));
			return convertirTerceroItemASolicitante(terceroItem);
		}
	
		return null;
	}
	
	// Ordenar Terceros por fecha de Actualizacion
	public class ComparadorFechaTerceroListItem implements Comparator<TerceroListItem> {
	    @Override
	    public int compare(TerceroListItem o1, TerceroListItem o2) {
	        return o2.getFechaActualizacion().compare(o1.getFechaActualizacion());
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
	
	private String crearTerceroMinimal(TerceroMinimalItem tercero) throws TercerosServiceException{
		try {
			return tercerosPort.crearTerceroMinimal(tercero);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al intentar crear un Tercero en Platino: "+tercero.getNumeroDocumento()+" - "+e.getMessage(), e);
		}
	}
	
	public String crearTerceroMinimal(Solicitante tercero) throws TercerosServiceException{
		return crearTerceroMinimal(convertirSolicitanteATerceroMinimal(tercero));
	}
	
	private TerceroMinimalItem convertirSolicitanteATerceroMinimal (Solicitante solicitante){
		if (solicitante != null) {
			TerceroMinimalItem tercero = new TerceroMinimalItem();
			tercero.setNumeroDocumento(solicitante.getNumeroId());
			if (solicitante.isPersonaFisica()){
				tercero.setTipoDocumento(convertirTipoNipATipoDocumentoItem(solicitante.fisica.nip.tipo));
				TipoTerceroItem tipo = new TipoTerceroItem();
				tipo.setId("FISICO");
				tercero.setTipoTercero(tipo);
				tercero.setNombre(solicitante.fisica.nombre);
				tercero.setApellido1(solicitante.fisica.primerApellido);
				if (solicitante.fisica.segundoApellido != null) {
					tercero.setApellido2(solicitante.fisica.segundoApellido);
				}
			} else {
				tercero.setTipoDocumento(convertirTipoNipATipoDocumentoItem("cif"));
				TipoTerceroItem tipo = new TipoTerceroItem();
				tipo.setId("JURIDICO");
				tercero.setTipoTercero(tipo);
				tercero.setNombre(solicitante.juridica.entidad);
			}
			tercero.setFechaCreacion(XMLGregorianCalendarConverter.asXMLGregorianCalendar(new DateTime()));
			return tercero;
		} else {
			return null;
		}
	}

	private TipoDocumentoItem convertirTipoNipATipoDocumentoItem (String tipoNipCif){
		TipoDocumentoItem ret = new TipoDocumentoItem();
		if ("nif".equalsIgnoreCase(tipoNipCif)){
			ret.setId("NIF");
		} else if ("nie".equalsIgnoreCase(tipoNipCif)){
			ret.setId("NIE");
		} else if ("cif".equalsIgnoreCase(tipoNipCif)){ 
			ret.setId("CIF");
		} else { // Pasaporte
			ret.setId("PASAPORTE");
		}
		return ret;
	}

	/**
	 * Método para mapear los datos de tercero del objeto TerceroItem a la clase Solicitante de FAP
	 * @param tercero 
	 * @return Solicitante Devuelve un objeto solicitante(FAP)
	 */

	private Solicitante convertirTerceroItemASolicitante(TerceroItem tercero) throws TercerosServiceException{

		Solicitante  s= null;
		if(tercero!=null){
			s = new Solicitante();
			s.uriTerceros=tercero.getUri();
			if(tercero.getTipoTercero().getId().equalsIgnoreCase("JURIDICO") || tercero.getTipoTercero().getId().equalsIgnoreCase("ORGANISMO")){
				s.tipo="juridica";
				s.juridica.cif = tercero.getNumeroDocumento();
				s.juridica.entidad = tercero.getNombre();
				if(tercero.getEmails()!=null &&tercero.getEmails().size()>0)
				{	
					EmailItem correo = buscarCorreoPrincipal(tercero.getEmails());
					if(correo!=null){
						s.juridica.email = correo.getDireccion();
					}
				}

			}else if(tercero.getTipoTercero().getId().equalsIgnoreCase("FISICO")){
				s.tipo="fisica";
				if(tercero.getTipoDocumento()!=null)
					s.fisica.nip.tipo = tercero.getTipoDocumento().getId();
				s.fisica.nip.valor=tercero.getNumeroDocumento();
				s.fisica.nombre = tercero.getNombre();
				s.fisica.primerApellido = tercero.getApellido1();
				s.fisica.segundoApellido = tercero.getApellido2();
				if (tercero.getFechaNacimiento() != null)
					s.fisica.fechaNacimiento = XMLGregorianCalendarConverter.asDatetime(tercero.getFechaNacimiento());
				if (tercero.getSexo() != null)
					s.fisica.sexo=tercero.getSexo().getId().toLowerCase();
				
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
							s.domicilio.municipio="_"+getCodigoMuncipioFapFromTerceros(mun.getIdProvincia(), mun.getId(), mun.getDigitoControl());
						s.domicilio.numero=d.getPortal();
						s.domicilio.otros="";
						if (d.getBloque() != null)
							s.domicilio.otros+="Bloque: "+d.getBloque();
						if (d.getEscalera() != null){
							if (!s.domicilio.otros.isEmpty())
								s.domicilio.otros+=", ";
							s.domicilio.otros+="Escalera: "+d.getEscalera();
						}
						if (d.getPiso() != null){
							if (!s.domicilio.otros.isEmpty())
								s.domicilio.otros+=", ";
							s.domicilio.otros+="Piso: "+d.getPiso();
						}
						if (d.getPuerta() != null){
							if (!s.domicilio.otros.isEmpty())
								s.domicilio.otros+=", ";
							s.domicilio.otros+="Puerta: "+d.getPuerta();
						} if (d.getOtros()!= null){
							if (!s.domicilio.otros.isEmpty())
								s.domicilio.otros+=", ";
							s.domicilio.otros+=d.getBloque();
						}
						PaisItem pais = recuperarPais(d.getIdPais());
						if(pais!=null)
							s.domicilio.pais="_"+getCodigoPaisFapFromTerceros(pais.getId());
						ProvinciaItem prov = recuperarProvincia(d.getIdProvincia());
						if(prov!=null){
							s.domicilio.provincia="_"+getCodigoProvinciaFapFromTerceros(prov.getId());
							s.domicilio.comunidad="_"+convertirProvinciaAComunidadAutonoma(getCodigoProvinciaFapFromTerceros(prov.getId()));
						}
						s.domicilio.provinciaInternacional=d.getEstado();
						IslaItem isla = recuperarIsla(d.getIdIsla());
						if (isla!=null)
							s.domicilio.isla="_"+getCodigoIslaFapFromTerceros(isla.getId());
					}
				}
			}

			//Existe posibilidad de recibir varios número de teléfono si tener claro cuantos de cada tipo puede almacenarse
			//en la base de datos de tercero. En cualquier caso FAP solo admite uno por tipo en el objeto de la clase
			//solicitante
			if(tercero.getTelefonos()!=null && tercero.getTelefonos().size()>0){
				TelefonoItem tfno = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"FIJO");
				String tfnoContacto = null;
				if(tfno!=null){
					s.telefonoFijo = tfno.getNumero();
					tfnoContacto = tfno.getNumero();
				}

				TelefonoItem tfnoMovil = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"MOVIL");
				if(tfnoMovil!=null){
					s.telefonoMovil = tfnoMovil.getNumero();
					if (tfnoContacto == null)
						tfnoContacto = tfnoMovil.getNumero();
				}

				TelefonoItem fax = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"FAX");
				if(fax!=null)
					s.fax = fax.getNumero();
				if (tfnoContacto != null)
					s.telefonoContacto=tfnoContacto;
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
	
	private String getCodigoMuncipioFapFromTerceros (Long idProvincia, Long idMunicipio, String idCodigoControl){
		String ret="";
		ret = String.format("%02d", idProvincia)+String.format("%03d", idMunicipio)+idCodigoControl;
		return ret;
	}
	
	private String getCodigoProvinciaFapFromTerceros (Long idProvincia){
		String ret="";
		ret = String.format("%02d", idProvincia);
		return ret;
	}
	
	private String convertirProvinciaAComunidadAutonoma(String idProvincia){
		if (idProvincia == null)
			return null;
		if (idProvincia.equals("04") || idProvincia.equals("11") || idProvincia.equals("14") || idProvincia.equals("18") || idProvincia.equals("21") || idProvincia.equals("23") || idProvincia.equals("29") || idProvincia.equals("41"))
			return "01";
		else if (idProvincia.equals("22") || idProvincia.equals("44") || idProvincia.equals("50"))
			return "02";
		else if (idProvincia.equals("33"))
			return "03";
		else if (idProvincia.equals("07"))
			return "04";
		else if (idProvincia.equals("35") || idProvincia.equals("38"))
			return "05";
		else if (idProvincia.equals("39"))
			return "06";
		else if (idProvincia.equals("05") || idProvincia.equals("09") || idProvincia.equals("24") || idProvincia.equals("34") || idProvincia.equals("37") || idProvincia.equals("40") || idProvincia.equals("42") || idProvincia.equals("47") || idProvincia.equals("49"))
			return "07";
		else if (idProvincia.equals("02") || idProvincia.equals("13") || idProvincia.equals("16") || idProvincia.equals("19") || idProvincia.equals("45"))
			return "08";
		else if (idProvincia.equals("08") || idProvincia.equals("17") || idProvincia.equals("25") || idProvincia.equals("43"))
			return "09";
		else if (idProvincia.equals("03") || idProvincia.equals("12") || idProvincia.equals("46"))
			return "10";
		else if (idProvincia.equals("06") || idProvincia.equals("10"))
			return "11";
		else if (idProvincia.equals("15") || idProvincia.equals("27") || idProvincia.equals("32") || idProvincia.equals("36"))
			return "12";
		else if (idProvincia.equals("28"))
			return "13";
		else if (idProvincia.equals("30"))
			return "14";
		else if (idProvincia.equals("31"))
			return "15";
		else if (idProvincia.equals("01") || idProvincia.equals("20") || idProvincia.equals("48"))
			return "16";
		else if (idProvincia.equals("26"))
			return "17";
		else if (idProvincia.equals("51"))
			return "18";
		else if (idProvincia.equals("52"))
			return "19";
		return null;
	}
	
	private String getCodigoPaisFapFromTerceros (Long idPais){
		if (idPais == null)
			return null;
		if (idPais == 64) // España
			return "724";
		return null;
	}
	
	private String getCodigoIslaFapFromTerceros (Long idIsla){
		if (idIsla == null)
			return null;
		if (idIsla == 10) // Lanzarote
			return "353";
		else if (idIsla == 20) // Fuerteventura
			return "351";
		else if (idIsla == 30) // Gran Canaria
			return "352";
		else if (idIsla == 40) // Tenerife
			return "384";
		else if (idIsla == 50) // La Gomera
			return "381";
		else if (idIsla == 60) // La Palma
			return "383";
		else if (idIsla == 70) // El Hierro
			return "382";
		return null;
	}

	@Override
	public Agente buscarTercerosAgenteByNumeroIdentificacion (String numeroIdentificacion, String tipoIdentificacion)
			throws TercerosServiceException {
		TerceroMinimalItem tercero = new TerceroMinimalItem();
		tercero.setNumeroDocumento(numeroIdentificacion);
		tercero.setTipoDocumento(convertirTipoNipATipoDocumentoItem(tipoIdentificacion));

		List<TerceroListItem> tercerosListItem = buscarTercerosDetalladosByItem(tercero);

		if ((tercerosListItem != null) && (!tercerosListItem.isEmpty())) {
			Collections.sort(tercerosListItem,
					new ComparadorFechaTerceroListItem());
			TerceroItem terceroItem = consultarTercero(tercerosListItem.get(0));
			return convertirTerceroItemAAgente(terceroItem);
		}

		return null;

	}

	/**
	 * Método para mapear los datos de tercero del objeto TerceroItem a la clase
	 * Agente de FAP
	 * 
	 * @param tercero
	 * @return Agente Devuelve un objeto Agente(FAP)
	 */
	private Agente convertirTerceroItemAAgente(TerceroItem tercero) throws TercerosServiceException {
		
		Agente agente = null;
		if (tercero != null) {
			// Primero lo busca por si ya existe en la BBDD
			agente = Agente.find("select agente from Agente agente where agente.username=?", tercero.getNumeroDocumento()).first();
			if (agente == null)
				agente = new Agente();
			agente.username = tercero.getNumeroDocumento();
			agente.name = tercero.getNombre() + " " + tercero.getApellido1() + " " + tercero.getApellido2();
			if ((tercero.getEmails() != null)
					&& (tercero.getEmails().size() > 0)) {
				EmailItem correo = buscarCorreoPrincipal(tercero.getEmails());
				if (correo != null) {
					agente.email = correo.getDireccion();
				}
			}
		}
		// TODO: Añadir más campos al agente
		return agente;
	}
}
