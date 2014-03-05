package services.comunicacionesInternas;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import java.util.regex.Matcher;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import messages.Messages;
import models.ReturnUnidadOrganicaFap;

import platino.PlatinoProxy;
import platino.PlatinoSecurityUtils;
import properties.FapProperties;
import properties.PropertyPlaceholder;

import swhiperreg.ciservices.ReturnError;
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import swhiperreg.service.ReturnUnidadOrganica;
import swhiperreg.service.Service;
import swhiperreg.service.ServiceSoap;
import utils.ComunicacionesInternasUtils;
import utils.WSUtils;


public class ServiciosGenericosServiceImpl {
	private ServiceSoap genericosServices;
	private PropertyPlaceholder propertyPlaceholder;
	
	@Inject
	public ServiciosGenericosServiceImpl (PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
		URL wsdlURL = ServiciosGenericosServiceImpl.class.getClassLoader().getResource("wsdl/Service.wsdl");
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
		return propertyPlaceholder.get("fap.services.genericos.comunicaciones.internas.url");
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
			String usuario = FapProperties.get("fap.platino.registro.username");
			String password = FapProperties.get("fap.platino.registro.password");
			//System.out.println("+++++++++++++++++++ PASSWORD Genericos: "+password);
			hasConnection = validarUsuario(usuario, password);
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "?: "+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}

	public List<ReturnUnidadOrganicaFap> consultaUnidadesOrganicas(String userId, String password){
		ArrayOfReturnUnidadOrganica resultado = genericosServices.obtenerUnidadesOrganicas(0, userId, password);
		 ReturnUnidadOrganica unidadorganica = resultado.getReturnUnidadOrganica().get(0);
		 System.out.println("codigo: " + unidadorganica.getCodigo());
		 System.out.println("codigo completo: " + unidadorganica.getCodigoCompleto());
		 System.out.println("descripcion: " + unidadorganica.getDescripcion());
		 System.out.println("es baja: " + unidadorganica.getEsBaja());
		 System.out.println("es receptora: " + unidadorganica.getEsReceptora());
		 System.out.println("codigo receptora: " + unidadorganica.getCodigoUOReceptora());
		 System.out.println("error: " + unidadorganica.getError().toString());
		 return ComunicacionesInternasUtils.returnUnidadOrganica2returnUnidadOrganicaFap(resultado);
	}

	public boolean validarUsuario (String userId, String password){
		play.Logger.info("Intentando validar usuario "+userId+" en Hiperreg con password: "+password);
		String resultado = "";
		try {
			resultado = genericosServices.validarUsuario(userId, encriptarPassword(password));
		} catch (Exception e) {
			play.Logger.error("Error comprobando la validez del usuario: "+userId+" en Hiperreg");
		}
		Pattern patron = Pattern.compile("<MensajeError>(.*?)</MensajeError>");
		Matcher matcher = patron.matcher(resultado);
		
		if ((matcher.find() || resultado.isEmpty())){ //Hay error
			play.Logger.error(matcher.group(1));
			Messages.error("Error validando el usuario en Hiperreg: "+matcher.group(2));
			return false;
		} else {
			play.Logger.info("Validación correcta del usuario "+userId+" en Comunicaciones Internas");
			return true;
		}
		
	}
	
	private String encriptarPassword(String password){
        try {
            return PlatinoSecurityUtils.encriptarPasswordComunicacionesInternas(password);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando la contraseña");
        }	    
	}
}
