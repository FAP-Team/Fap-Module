package services.comunicacionesInternas;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.tools.corba.common.WSDLUtils;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import config.InjectorConfig;
import controllers.fap.AgenteController;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaCriteriaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;
import es.gobcan.platino.servicios.registro.Asunto;
import es.gobcan.platino.servicios.sfst.FirmaService;
import platino.PlatinoProxy;
import platino.PlatinoSecurityUtils;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import messages.Messages;
import models.Agente;
import models.AsientoAmpliadoCIFap;
import models.AsientoCIFap;
import models.ListaUris;
import models.RespuestaCIAmpliadaFap;
import models.RespuestaCIFap;
import models.ReturnUnidadOrganicaFap;
import services.ComunicacionesInternasService;
import services.ComunicacionesInternasServiceException;
import services.ServiciosGenericosService;
import services.VerificarDatosServiceException;
import services.platino.PlatinoBDOrganizacionServiceImpl;
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
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import utils.ComunicacionesInternasUtils;
import utils.WSUtils;
import utils.GestorDocumentalUtils;

@InjectSupport
public class ComunicacionesInternasServiceImpl implements ComunicacionesInternasService{

	private CIServicesSoap comunicacionesServices;
	private PropertyPlaceholder propertyPlaceholder;
	private PlatinoGestorDocumentalService platinoGestorDocumental;
	
	@Inject
	private ServiciosGenericosService genericosService;
	
	private final String URIPROCEDIMIENTO;
	private final String TIPO_TRANSPORTE;
	public final String USUARIOHIPERREG;
	public final String PASSWORDHIPERREG;
	
	@Inject
	public ComunicacionesInternasServiceImpl (PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		URL wsdlURL = ComunicacionesInternasService.class.getClassLoader().getResource("wsdl/CIServices.wsdl");
		comunicacionesServices = new CIServices(wsdlURL).getCIServicesSoap();
		WSUtils.configureEndPoint(comunicacionesServices, getEndPoint());
		
		USUARIOHIPERREG = FapProperties.get("fap.platino.registro.username");
		PASSWORDHIPERREG = FapProperties.get("fap.platino.registro.password");
		URIPROCEDIMIENTO = FapProperties.get("fap.platino.security.procedimiento.uri");
		TIPO_TRANSPORTE = FapProperties.get("fap.platino.registro.tipoTransporte");
		
	    Map<String, String> headers = null;
        
        if ((URIPROCEDIMIENTO != null) && (URIPROCEDIMIENTO.compareTo("undefined") != 0)) {
        	headers = new HashMap<String, String>();
        	headers.put("uriProcedimiento", URIPROCEDIMIENTO);		
        }
		
        WSUtils.configureSecurityHeaders(comunicacionesServices, propertyPlaceholder, headers);
		
        //El servicio de comunicaciones internas y servicios genéricos no funciona con proxy, entra en conflicto.
        //Por esto hay que ponerlo a falso, antes de configurar sus políticas.
        boolean proxyEnable = FapProperties.getBoolean("fap.proxy.enable");
        FapProperties.setBoolean("fap.proxy.enable", false);

        PlatinoProxy.setProxy(comunicacionesServices, propertyPlaceholder);
        
		platinoGestorDocumental = InjectorConfig.getInjector().getInstance(PlatinoGestorDocumentalService.class);
		
		//Se deja al proxy con el valor que tenía antes de inyectar el servicio de Comunicaciones Internas
	    FapProperties.setBoolean("fap.proxy.enable", proxyEnable);

	}
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.services.comunicaciones.internas.url");
	}
	
	// TODO: revisar que no está completo.
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = genericosService.validarUsuario(USUARIOHIPERREG, PASSWORDHIPERREG);
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "?: "+hasConnection);
		}catch(Exception e){
			e.printStackTrace();
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
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con Hiperreg y está operativo.");
		else
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con Hiperreg y NO está operativo.");
    }

	/**
	 * Método que crea un nuevo asiento de una comunicación interna.
	 */
	@Override
	public RespuestaCIFap crearNuevoAsiento(AsientoCIFap asientoFap) throws ComunicacionesInternasServiceException {
		
		ArrayOfString listaUris = null;
		try{
			listaUris = obtenerUriPlatino(asientoFap.uris);
		}catch(Exception e){
			play.Logger.error("Se ha producido el error obteniendo las uri de los documentos de platino: " + e.getMessage(), e);
			throw new ComunicacionesInternasServiceException("No se ha podido recuperar las uris de los documentos de platino");
		}
		
		try{	        	  
			ReturnComunicacionInterna respuesta= null;
			RespuestaCIFap respuestafap = null;
			if (!Messages.hasErrors()) {
				respuesta = comunicacionesServices.nuevoAsiento(asientoFap.observaciones, 
						asientoFap.resumen,
						asientoFap.numeroDocumentos,
						asientoFap.interesado,
						asientoFap.unidadOrganicaDestino.codigo,
						asientoFap.asuntoCodificado,
						asientoFap.userId,
						encriptarPassword(asientoFap.password),
						asientoFap.tipoTransporte,
						listaUris);
			
				respuestafap = ComunicacionesInternasUtils.respuestaComunicacionInterna2respuestaComunicacionInternaFap(respuesta);
			}
			
			return respuestafap;
		}
		catch(Exception e){
			play.Logger.error("Se ha producido un error al enviar la comunicación interna: " + e.getMessage(), e);
			throw new ComunicacionesInternasServiceException("Se ha producido un error al enviar la comunicación interna");
		}
	}
	
	/**
	 * Método que crea un nueva asiento de una comunicación interna con el añadido de la unidad orgánica de origen.
	 */
	@Override
	public RespuestaCIAmpliadaFap crearNuevoAsientoAmpliado(AsientoAmpliadoCIFap asientoAmpliadoFap) throws ComunicacionesInternasServiceException{
		
		ArrayOfString listaUris = null;
		try{
			listaUris = obtenerUriPlatino(asientoAmpliadoFap.uris);
		}catch(Exception e){
			play.Logger.error("Se ha producido el error obteniendo las uri de los documentos de platino: " + e.getMessage(), e);
			throw new ComunicacionesInternasServiceException("No se ha podido recuperar las uris de los documentos de platino");
		}
		
		try{
			ReturnComunicacionInternaAmpliada respuesta = null;
			RespuestaCIAmpliadaFap respuestafap = null;
			if (!Messages.hasErrors()) {
					comunicacionesServices.nuevoAsientoAmpliado(
						asientoAmpliadoFap.observaciones, 
						asientoAmpliadoFap.resumen,
						asientoAmpliadoFap.numeroDocumentos,
						asientoAmpliadoFap.interesado,
						asientoAmpliadoFap.unidadOrganicaDestino.codigo,
						asientoAmpliadoFap.asuntoCodificado,
						asientoAmpliadoFap.userId,
						encriptarPassword(asientoAmpliadoFap.password),
						asientoAmpliadoFap.tipoTransporte,
						listaUris,
						asientoAmpliadoFap.unidadOrganicaOrigen.codigo);
			
					respuestafap = ComunicacionesInternasUtils.respuestaComunicacionInternaAmpliada2respuestaComunicacionInternaAmpliadaFap(respuesta);
			}
			
			return respuestafap;
		}
		catch(Exception e){
			play.Logger.error("Se ha producido un error al enviar la comunicación interna: " + e.getMessage(), e);
			throw new ComunicacionesInternasServiceException("Se ha producido un error al enviar la comunicación interna");
		}
	}
	
	/**
	 * Método que comprueba si un documento está en platino, que en caso contrario si existe en el aed de aciisi lo sube a platino,
	 * y devuelve la uri del aed de platino.
	 * @param uris
	 * @return
	 */
	private ArrayOfString obtenerUriPlatino(List<ListaUris> uris) {
		ArrayOfString listaUris = null;
		
		for (int i = 0; i < uris.size(); i++){
			String uriPlatino = platinoGestorDocumental.obtenerURIPlatino(uris.get(i).uri, PlatinoGestorDocumentalService.class);
			if ((uriPlatino != null) && (!uriPlatino.isEmpty())) {
				if (listaUris == null)
					listaUris = new ArrayOfString();
				listaUris.getString().add(uriPlatino);
			}
			else {
				play.Logger.error("Error al obtener la uri de platino del documento con uri "+ uris.get(i).uri);
				Messages.error("Error al obtener la uri de platino del documento con uri "+ uris.get(i).uri);
			}
		}
		
		return listaUris;
	}

}
