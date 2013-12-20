package services.comunicacionesInternas;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import platino.PlatinoProxy;
import properties.FapProperties;
import properties.PropertyPlaceholder;

import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import swhiperreg.service.Service;
import swhiperreg.service.ServiceSoap;
import utils.ComunicacionesInternasUtils;
import utils.WSUtils;


public class ServiciosGenericosServiceImpl {
	private ServiceSoap genericosServices;
	private PropertyPlaceholder propertyPlaceholder;
	
	public ServiciosGenericosServiceImpl (PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
		URL wsdlURL = ServiciosGenericosServiceImpl.class.getClassLoader().getResource("wsdl/com-internas.wsdl");
		genericosServices = new Service(wsdlURL).getServiceSoap();
		WSUtils.configureEndPoint(genericosServices, getEndPoint());
        WSUtils.configureSecurityHeaders(genericosServices, propertyPlaceholder);
        PlatinoProxy.setProxy(genericosServices, propertyPlaceholder);
        
        Client client = ClientProxy.getClient(genericosServices);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
	}
	
	private String getEndPoint() {
		return FapProperties.get("fap.services.genericos.comunicaciones.internas.url");
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}

    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio Genérico de Comunicaciones Internas ha sido inyectado y está operativo.");
		else
			play.Logger.info("El servicio Genérico de Comunicaciones Internas ha sido inyectado y NO está operativo.");
    }
	
	
	// TODO: revisar que no está completo.
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			String usuario = FapProperties.get("fap.fap.platino.registro.username");
			String password = password2utf16(FapProperties.get("fap.platino.registro.password"));
			System.out.println("+++++++++++++++++++ PASSWORD: "+password);
			hasConnection = validarUsuario(usuario, password); //QUE USO AQUI??
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "?: "+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}

	public List<String> consultaUnidadesOrganicas(String userId, String password){
		ArrayOfReturnUnidadOrganica resultado = genericosServices.obtenerUnidadesOrganicas(0, userId, password);
		return ComunicacionesInternasUtils.ArrayOfReturnUnidadOrganica2List(resultado);
	}

	public boolean validarUsuario (String userId, String password){
		String usuario = genericosServices.validarUsuario(userId, password);
		if (!usuario.isEmpty()){
			return false;
		}
		return true;
	}
	
	public String password2utf16(String password){
		//Comprobamos que el password está en UTF-16
		String password16="";
		try {
			password16= new String(password.getBytes(), "UTF-16");
		} catch (UnsupportedEncodingException e) {
			play.Logger.error("Error tranformando la contraseña de usuario a UTF-16: "+password);
			e.printStackTrace();
		}
		return password16;
	}
}
