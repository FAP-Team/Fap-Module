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

import config.InjectorConfig;
import controllers.fap.AgenteController;

import es.gobcan.platino.servicios.sfst.FirmaService;

import platino.PlatinoProxy;
import platino.PlatinoSecurityUtils;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import models.Agente;
import models.AsientoAmpliadoCIFap;
import models.AsientoCIFap;
import models.ReturnComunicacionInternaAmpliadaFap;
import models.ReturnComunicacionInternaFap;
import models.ReturnUnidadOrganicaFap;
import services.ComunicacionesInternasService;
import services.ComunicacionesInternasServiceException;
import services.VerificarDatosServiceException;
import services.platino.PlatinoFirmaServiceImpl;
import services.platino.PlatinoGestorDocumentalService;
import swhiperreg.ciservices.ArrayOfString;
import swhiperreg.ciservices.CIServices;
import swhiperreg.ciservices.CIServicesSoap;
import swhiperreg.ciservices.NuevoAsiento;
import swhiperreg.ciservices.ReturnComunicacionInterna;
import swhiperreg.ciservices.ReturnComunicacionInternaAmpliada;
import swhiperreg.entradaservices.EntradaServices;
import swhiperreg.entradaservices.ReturnEntrada;
import utils.ComunicacionesInternasUtils;
import utils.WSUtils;
import utils.GestorDocumentalUtils;

@InjectSupport
public class ComunicacionesInternasServiceImpl implements ComunicacionesInternasService{

	private CIServicesSoap comunicacionesServices;
	private ServiciosGenericosServiceImpl genericosService;
	private PropertyPlaceholder propertyPlaceholder;
	private PlatinoGestorDocumentalService platinoGestorDocumental;
	
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
		
		platinoGestorDocumental = InjectorConfig.getInjector().getInstance(PlatinoGestorDocumentalService.class);
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
			String usuario = FapProperties.get("fap.platino.registro.username");
			String password = FapProperties.get("fap.platino.registro.password");
			hasConnection = genericosService.validarUsuario(usuario, password);
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
	public ReturnComunicacionInternaFap crearNuevoAsiento(AsientoCIFap asientoFap) throws ComunicacionesInternasServiceException {
		ArrayOfString listaUris = new ArrayOfString();
		
		for (int i = 0; i < asientoFap.uris.size(); i++){
			String uriPlatino = platinoGestorDocumental.obtenerURIPlatino(asientoFap.uris.get(i).uri, comunicacionesServices);
			if ((uriPlatino != null) && (!uriPlatino.isEmpty()))
				listaUris.getString().add(uriPlatino);
			else
				play.Logger.error("Error al obtener la uri de platino del documento con uri "+asientoFap.uris.get(i).uri);
		}
		
		try{
		ReturnComunicacionInterna respuesta = comunicacionesServices.nuevoAsiento(asientoFap.observaciones, 
				asientoFap.resumen,
				asientoFap.numeroDocumentos,
				asientoFap.interesado,
				asientoFap.unidadOrganicaDestino.codigo,
				asientoFap.asuntoCodificado,
				asientoFap.userId,
				asientoFap.password,
				asientoFap.tipoTransporte,
				listaUris);
		System.out.println(respuesta.getUsuario().toString());
		
		return ComunicacionesInternasUtils.respuestaComunicacionInterna2respuestaComunicacionInternaFap(respuesta);
		}
		catch(Exception e){
			play.Logger.error("Se ha producido el error: " + e.getMessage(), e);
			throw new ComunicacionesInternasServiceException("No se ha podido obtener respuesta");
		}
	}
	
	public ReturnComunicacionInternaAmpliadaFap crearNuevoAsientoAmpliado(AsientoAmpliadoCIFap asientoAmpliadoFap) throws ComunicacionesInternasServiceException{

		ArrayOfString listaUris = new ArrayOfString();
		
		for (int i = 0; i < asientoAmpliadoFap.uris.size(); i++){
			String uriPlatino = platinoGestorDocumental.obtenerURIPlatino(asientoAmpliadoFap.uris.get(i).uri, comunicacionesServices);
			if ((uriPlatino != null) && (!uriPlatino.isEmpty()))
				listaUris.getString().add(uriPlatino);
			else
				play.Logger.error("Error al obtener la uri de platino del documento con uri "+asientoAmpliadoFap.uris.get(i).uri);
		}
		
		try{
			ReturnComunicacionInternaAmpliada respuesta = comunicacionesServices.nuevoAsientoAmpliado(
					asientoAmpliadoFap.observaciones, 
					asientoAmpliadoFap.resumen,
					asientoAmpliadoFap.numeroDocumentos,
					asientoAmpliadoFap.interesado,
					asientoAmpliadoFap.unidadOrganicaDestino.codigo,
					asientoAmpliadoFap.asuntoCodificado,
					asientoAmpliadoFap.userId,
					asientoAmpliadoFap.password,
					asientoAmpliadoFap.tipoTransporte,
					listaUris,
					asientoAmpliadoFap.unidadOrganicaOrigen.codigo);
			
			return ComunicacionesInternasUtils.respuestaComunicacionInternaAmpliada2respuestaComunicacionInternaAmpliadaFap(respuesta);
		}
		catch(Exception e){
			play.Logger.error("Se ha producido el error: " + e.getMessage(), e);
			throw new ComunicacionesInternasServiceException("No se ha podido obtener respuesta");
		}
	}

	@Override
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicas(String userId, String password){
		return this.genericosService.consultaUnidadesOrganicas(userId, encriptarPassword(password));
	}
	
	//TODO poner privado y quitar del service
	public String encriptarPassword(String password){
        try {
            return PlatinoSecurityUtils.encriptarPasswordComunicacionesInternas(password);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando la contraseña");
        }	    
	}
}
