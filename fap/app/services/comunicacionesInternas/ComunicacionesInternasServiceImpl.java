package services.comunicacionesInternas;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.tools.corba.common.WSDLUtils;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import es.gobcan.platino.servicios.sfst.FirmaService;

import platino.PlatinoProxy;
import platino.PlatinoSecurityUtils;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import models.AsientoCIFap;
import models.ReturnComunicacionInternaFap;
import services.ComunicacionesInternasService;
import services.platino.PlatinoFirmaServiceImpl;
import swhiperreg.ciservices.ArrayOfString;
import swhiperreg.ciservices.CIServices;
import swhiperreg.ciservices.CIServicesSoap;
import swhiperreg.ciservices.NuevoAsiento;
import swhiperreg.ciservices.ReturnComunicacionInterna;
import swhiperreg.entradaservices.EntradaServices;
import swhiperreg.entradaservices.ReturnEntrada;
import utils.ComunicacionesInternasUtils;
import utils.WSUtils;

public class ComunicacionesInternasServiceImpl implements ComunicacionesInternasService{

	private CIServicesSoap comunicacionesServices;
	private ServiciosGenericosServiceImpl genericosService;
	private PropertyPlaceholder propertyPlaceholder;
	
	@Inject
	public ComunicacionesInternasServiceImpl (PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		URL wsdlURL = ComunicacionesInternasService.class.getClassLoader().getResource("wsdl/CIServices.wsdl");
		comunicacionesServices = new CIServices(wsdlURL).getCIServicesSoap();
		WSUtils.configureEndPoint(comunicacionesServices, getEndPoint());
        WSUtils.configureSecurityHeaders(comunicacionesServices, propertyPlaceholder);
        PlatinoProxy.setProxy(comunicacionesServices, propertyPlaceholder);
        
        Client client = ClientProxy.getClient(comunicacionesServices);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
		
		genericosService = new ServiciosGenericosServiceImpl(propertyPlaceholder);
		genericosService.mostrarInfoInyeccion();
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con Hiperreg y está operativo.");
		else
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con Hiperreg y NO está operativo.");
    }
	
	
	// TODO: revisar que no está completo.
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			String usuario = FapProperties.get("fap.fap.platino.registro.username");
			String password = FapProperties.get("fap.platino.registro.password");
			//hasConnection = genericosService.validarUsuario(usuario, password);
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "?: "+hasConnection);
		}catch(Exception e){
			e.printStackTrace();
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.services.comunicaciones.internas.url");
	}


	@Override
	public ReturnComunicacionInternaFap crearNuevoAsiento(AsientoCIFap asientoFap) {
		ArrayOfString listaUris = new ArrayOfString();
		
		for (int i = 0; i < asientoFap.uris.size(); i++){
			listaUris.getString().add(asientoFap.uris.get(i).uri);
		}
		
		return ComunicacionesInternasUtils.comunicacionInterna2ComunicacionInternaFap(
				comunicacionesServices.nuevoAsiento(asientoFap.observaciones, 
											asientoFap.resumen,
											asientoFap.numeroDocumentos,
											asientoFap.interesado,
											asientoFap.unidadOrganicaDestino,
											asientoFap.asuntoCodificado,
											asientoFap.userId,
											asientoFap.password,
											asientoFap.tipoTransporte,
											listaUris));
		
	}

	@Override
	public List<String> obtenerUnidadesOrganicas(String userId, String password){
		System.out.println("Llamando a CI de Hyperreg");
		return this.genericosService.consultaUnidadesOrganicas(userId, encriptarPassword(password));
	}
	
//	public String password2utf16(String password){
//		//Comprobamos que el password está en UTF-16
//		String password16="";
//		password="PLATIN";
//		try {
//			password16 = new String(password.getBytes(), "UTF-16LE");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		int codePoint = Character.codePointAt(password16, 1);
//		//System.out.println("codePoint: "+Integer.toHexString(codePoint));
//		return password16;
//	}
	
	private String encriptarPassword(String password){
        try {
            return PlatinoSecurityUtils.encriptarPassword(password);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando la contraseña");
        }	    
	}
}
