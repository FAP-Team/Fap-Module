package services.genericos;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import java.util.regex.Matcher;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import config.InjectorConfig;
import messages.Messages;
import models.ReturnUnidadOrganicaFap;
import platino.PlatinoProxy;
import platino.PlatinoSecurityUtils;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.platino.PlatinoGestorDocumentalService;
import swhiperreg.ciservices.ReturnError;
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import swhiperreg.service.ReturnUnidadOrganica;
import swhiperreg.service.Service;
import swhiperreg.service.ServiceSoap;
import utils.WSUtils;


public class ServiciosGenericosServiceImpl implements ServiciosGenericosService{
	
	private ServiceSoap genericosServices;
	private PropertyPlaceholder propertyPlaceholder;
	
	private final String URIPROCEDIMIENTO;
	public final String USUARIOHIPERREG;
	public final String PASSWORDHIPERREG;
	
	@Inject
	public ServiciosGenericosServiceImpl (PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
		URL wsdlURL = ServiciosGenericosServiceImpl.class.getClassLoader().getResource("wsdl/Service.wsdl");
		genericosServices = new Service(wsdlURL).getServiceSoap();
		WSUtils.configureEndPoint(genericosServices, getEndPoint());
		
		URIPROCEDIMIENTO = FapProperties.get("fap.platino.security.procedimiento.uri");
		USUARIOHIPERREG = FapProperties.get("fap.platino.registro.username");
	    PASSWORDHIPERREG = FapProperties.get("fap.platino.registro.password");   
		
	    Map<String, String> headers = null;       
        if ((URIPROCEDIMIENTO != null) && (URIPROCEDIMIENTO.compareTo("undefined") != 0)) {
        	headers = new HashMap<String, String>();
        	headers.put("uriProcedimiento", URIPROCEDIMIENTO);		
        }
	    
        WSUtils.configureSecurityHeaders(genericosServices, propertyPlaceholder);
        PlatinoProxy.setProxy(genericosServices, propertyPlaceholder);
	}
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.services.genericos.comunicaciones.internas.url");
	}
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = validarUsuario(USUARIOHIPERREG, PASSWORDHIPERREG);
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "?: "+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
	private String encriptarPassword(String password){
        try {
            return PlatinoSecurityUtils.encriptarPasswordComunicacionesInternas(password);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando la contraseña");
        }	    
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}

	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio Genérico de Comunicaciones Internas ha sido inyectado y está operativo.");
		else
			play.Logger.info("El servicio Genérico de Comunicaciones Internas ha sido inyectado y NO está operativo.");
    }
    
	@Override
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicas(Long codigo, String userId, String password) {
		ArrayOfReturnUnidadOrganica lstUOGenericos = null;
		List<ReturnUnidadOrganicaFap> lstUO = null;
		try {
			lstUOGenericos = genericosServices.obtenerUnidadesOrganicas(codigo, USUARIOHIPERREG, encriptarPassword(PASSWORDHIPERREG));
			if (lstUOGenericos != null)
				lstUO = ServiciosGenericosUtils.returnUnidadOrganica2returnUnidadOrganicaFap(lstUOGenericos);
		} catch (Exception e) {
			play.Logger.error("No se han podido recuperar las Unidades Orgánicas: " + e.getMessage());
		}
		
		return lstUO;
	}
	
	@Override
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicasV(Long codigo, String credencialesXml) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validarUsuario (String userId, String password){
		play.Logger.info("Intentando validar usuario "+userId+" en Hiperreg con password: "+password);
		String resultado = "";
		try {
			play.Logger.info("Usuario: " + userId + " Passwd: " + encriptarPassword(password));
			resultado = genericosServices.validarUsuario(userId, encriptarPassword(password));
		} catch (Exception e) {
			play.Logger.error("Error comprobando la validez del usuario: "+userId+" en Hiperreg");
		}
		Pattern patron = Pattern.compile("<MensajeError>(.*?)</MensajeError>");
		Matcher matcher = patron.matcher(resultado);
		
		if ((resultado.isEmpty() || matcher.find())){ //Hay error
			play.Logger.error(matcher.group(1));
			play.Logger.error("Error validando el usuario en Hiperreg: "+matcher.group(2));
			return false;
		} else {
			play.Logger.info("Validación correcta del usuario "+userId+" en Comunicaciones Internas");
			return true;
		}
		
	}

}
