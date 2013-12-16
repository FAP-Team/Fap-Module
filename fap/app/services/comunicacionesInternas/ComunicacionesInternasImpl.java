package services.comunicacionesInternas;

import java.net.URL;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.tools.corba.common.WSDLUtils;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import es.gobcan.platino.servicios.sfst.FirmaService;

import platino.PlatinoProxy;
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

public class ComunicacionesInternasImpl implements ComunicacionesInternasService{

	private CIServicesSoap comunicacionesServices;
	private PropertyPlaceholder propertyPlaceholder;
	
	public ComunicacionesInternasImpl (PropertyPlaceholder propertyPlaceholder){
		URL wsdlURL = PlatinoFirmaServiceImpl.class.getClassLoader().getResource("wsdl/com-internas.wsdl");
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
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado y está operativo.");
		else
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado y NO está operativo.");
    }
	
	
	// TODO: revisar que no está completo.
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = true; //QUE USO AQUI??  //getVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "?: "+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.entrada.comunicaciones.internas.url");
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
	
}
