package services.platino;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import es.gobcan.platino.servicios.edmyce.dominio.comun.ArrayOfCorreoElectronicoType;
import es.gobcan.platino.servicios.edmyce.dominio.comun.ArrayOfUriRemesaType;
import es.gobcan.platino.servicios.edmyce.dominio.comun.CanalMensajeEnumType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ArrayOfMensajeOficioType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ArrayOfMensajeType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeAreaType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeCriteriaType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeOficioType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.RemesaType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ResultadoBusquedaMensajeType;
import es.gobcan.platino.servicios.edmyce.mensajes.*;
import es.gobcan.platino.servicios.registro.Registro_Service;
import services.MensajeServiceException;
import utils.WSUtils;

@InjectSupport
public class PlatinoMensajeServiceImpl implements services.MensajeService {

	private PropertyPlaceholder propertyPlaceholder;
	
	private MensajePortType mensajePort;
	
	@Inject
	public PlatinoMensajeServiceImpl(PropertyPlaceholder propertyPlaceholder) {
		
        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoMensajeServiceImpl.class.getClassLoader().getResource("wsdl/mensaje.wsdl");
        mensajePort = new MensajeService(wsdlURL).getMensajeService();

        WSUtils.configureEndPoint(mensajePort, getEndPoint());
        WSUtils.configureSecurityHeaders(mensajePort, propertyPlaceholder);
        PlatinoProxy.setProxy(mensajePort, propertyPlaceholder);   
    }
    

	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Mensajes ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de Mensajes ha sido inyectado con Platino y NO está operativo.");
    }
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection =  getVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
    private String getVersion() {
        return mensajePort.getVersion();
    }
    
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.mensajes.url");
	}
	
	
	/**
	 * Realiza el envío de un mensaje a una dirección de correo (e-mail).
	 * 
	 * 	1. Se crea el mensaje
	 *  2. Se le adjunta el texto correspondiente
	 *  3. Se ñe adjunta un correo y se envía.
	 * 
	 * @param mensaje
	 * @param correo
	 * @return 
	 */
	@Override
	public  String enviarMensajeOficio (String mensaje, String correo) throws MensajeServiceException {
		
		MensajeOficioType mensajeDeOficio = new MensajeOficioType();
		
		CanalMensajeEnumType canal = CanalMensajeEnumType.EMAIL;
		mensajeDeOficio.setCanal(canal);
		
		mensajeDeOficio.setTextoEmail(mensaje);
		
		ArrayOfCorreoElectronicoType correos = new ArrayOfCorreoElectronicoType();
		correos.getCorreoElectronico().add(correo);
		mensajeDeOficio.setCorreosElectronicos(correos);
		
		try{
			return mensajePort.enviarMensajeOficio(mensajeDeOficio);
		}
		catch(Exception e){
			System.out.println("No se ha podido mandar el e-mail. Causa: " + e);
			throw new MensajeServiceException("No se pudo enviar el correo al email solicitado");
		}
	}
	
	@Override
	public  ArrayOfMensajeType obtenerMensajes (String uriRemesa) throws MensajeServiceException {
		
		
		
		try{
			return mensajePort.obtenerMensajes(uriRemesa); 
		}
		catch(Exception e){
			System.out.println("No se ha podido mandar el e-mail. Causa: " + e);
			throw new MensajeServiceException("No se pudo enviar el correo al email solicitado");
		}
	}
	
	/**
	 * Realiza el envío de un mensaje a varias direcciones de correo (e-mail).
	 * 
	 * 	1. Se crea el mensaje
	 *  2. Se le adjunta el texto correspondiente
	 *  3. Se le adjuntan las direcciones de correo y se envía.
	 * 
	 * @param mensaje
	 * @param correo
	 * @return 
	 */
	
	@Override
	public  String enviarMensajeOficioaVarios (String mensaje, List<String> correos) throws MensajeServiceException {
		
		MensajeOficioType mensajeDeOficio = new MensajeOficioType();
		
		CanalMensajeEnumType canal = CanalMensajeEnumType.EMAIL;
		mensajeDeOficio.setCanal(canal);
		
		mensajeDeOficio.setTextoEmail(mensaje);
		
		ArrayOfCorreoElectronicoType mails= new ArrayOfCorreoElectronicoType();
		for (String correo:correos){
			mails.getCorreoElectronico().add(correo);	
		}
		mensajeDeOficio.setCorreosElectronicos(mails);
		
		try{
			return mensajePort.enviarMensajeOficio(mensajeDeOficio);
		}
		catch(Exception e){
			System.out.println("No se ha podido mandar el e-mail. Causa: " + e);
			throw new MensajeServiceException("No se pudo enviar el correo al email solicitado");
		}
	}
	
	@Override
	public  ArrayOfUriRemesaType enviarMensajesOficio (List<String> mensajes, String correo) throws MensajeServiceException {
		
		ArrayOfMensajeOficioType mensajesDeOficio = new ArrayOfMensajeOficioType();

		MensajeOficioType message = new MensajeOficioType();
		
		CanalMensajeEnumType canal = CanalMensajeEnumType.EMAIL;
		
		ArrayOfCorreoElectronicoType correos = new ArrayOfCorreoElectronicoType();
		correos.getCorreoElectronico().add(correo);
		
		for (String mensaje:mensajes){
			message.setCanal(canal);
			message.setTextoEmail(mensajes.get(0));
			message.setCorreosElectronicos(correos);
			mensajesDeOficio.getMensajeOficio().add(message);	
		}
		try{
			return mensajePort.enviarMensajesOficio(mensajesDeOficio);
		}
		catch(MensajeException e){
			play.Logger.error("No se han podido mandar los correos. Causa: " + e.getMessage());
			throw new MensajeServiceException("No se están enviando los emails", e.getCause());
		}
	}
	
	@Override
	public ResultadoBusquedaMensajeType buscarMensaje (String via, String mail, Integer repeticiones) throws MensajeServiceException {
		
		MensajeCriteriaType criterioBusqueda = new MensajeCriteriaType();
		
		CanalMensajeEnumType canal = CanalMensajeEnumType.fromValue(via);
		
		criterioBusqueda.setCanal(canal);
		criterioBusqueda.setCorreoElectronico(mail);
		criterioBusqueda.setNumeroResultados(repeticiones);
		try{
			return mensajePort.buscarMensajes(criterioBusqueda);
		}
		catch(MensajeException e){
			play.Logger.error("La búsqueda no ha sido satisfactoria: " + e.getMessage());
			throw new MensajeServiceException("La búsqueda de mensajes no se ha ejecutado correctamente", e.getCause());
		}
	}
	
	
}
