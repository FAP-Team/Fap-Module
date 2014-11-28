package services.comunicacionesInternas;

import java.io.UnsupportedEncodingException;
import java.net.URL;
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
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import utils.ComunicacionesInternasUtils;
import utils.WSUtils;
import utils.GestorDocumentalUtils;

@InjectSupport
public class ComunicacionesInternasServiceImpl implements ComunicacionesInternasService{

	private CIServicesSoap comunicacionesServices;
	private ServiciosGenericosServiceImpl genericosService;
	private PropertyPlaceholder propertyPlaceholder;
	private PlatinoGestorDocumentalService platinoGestorDocumental;
	
	private final String URIPROCEDIMIENTO;
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
		genericosService = new ServiciosGenericosServiceImpl(propertyPlaceholder);
		genericosService.mostrarInfoInyeccion();

		//Se deja al proxy con el valor que tenía antes de inyectar el servicio de Comunicaciones Internas
	    FapProperties.setBoolean("fap.proxy.enable", proxyEnable);

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
			hasConnection = genericosService.validarUsuario(USUARIOHIPERREG, PASSWORDHIPERREG);
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

	private String encriptarPassword(String password){
        try {
            return PlatinoSecurityUtils.encriptarPasswordComunicacionesInternas(password);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando la contraseña");
        }	    
	}

	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicas(Long codigo) throws ComunicacionesInternasServiceException {
		ArrayOfReturnUnidadOrganica lstUOGenericos = null;
		List<ReturnUnidadOrganicaFap> lstUO = null;
		try {
			lstUOGenericos = genericosService.consultaUnidadesOrganicas(codigo, USUARIOHIPERREG, encriptarPassword(PASSWORDHIPERREG));
			if (lstUOGenericos != null)
				lstUO = ComunicacionesInternasUtils.returnUnidadOrganica2returnUnidadOrganicaFap(lstUOGenericos);
		} catch (Exception e) {
			play.Logger.error("No se han podido recuperar las Unidades Orgánicas");
			throw new ComunicacionesInternasServiceException("Excepción obteniendo unidades orgánicas");
		}
		
		return lstUO;
	}
	
}
