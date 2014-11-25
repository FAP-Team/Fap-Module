package services.platino;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import controllers.fap.AgenteController;
import platino.PlatinoProxy;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import models.DatosGenericosPeticionSVDFAP;
import models.FuncionarioSVDFAP;
import models.PeticionSVDFAP;
import models.PeticionSVDIdentidadFAP;
import models.ProcedimientoSVDFAP;
import models.SolicitanteSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import models.SolicitudesSVDFAP;
import models.TitularSVDFAP;
import enumerado.fap.gen.TipoConsentimientoEnum;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import es.gobcan.platino.servicios.svd.ScspwsService;
import es.gobcan.platino.servicios.svd.ScspwsService_Service;
import es.gobcan.platino.servicios.svd.SolicitudTransmision;
import es.gobcan.platino.servicios.svd.Solicitudes;
import es.gobcan.platino.servicios.svd.SvdException;
import es.gobcan.platino.servicios.svd.peticionconsentimiento.Consentimiento;
import es.gobcan.platino.servicios.svd.peticiondatosgenericos.DatosGenericos;
import es.gobcan.platino.servicios.svd.peticionfuncionario.Funcionario;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;
import es.gobcan.platino.servicios.svd.peticionprocedimiento.Procedimiento;
import es.gobcan.platino.servicios.svd.peticionsolicitante.Solicitante;
import es.gobcan.platino.servicios.svd.peticiontipodocumentacion.TipoDocumentacion;
import es.gobcan.platino.servicios.svd.peticiontitularpet.Titularpet;
import services.SVDService;
import services.VerificarDatosServiceException;
import utils.WSUtils;

public class PlatinoSVDImpl implements SVDService {

	
	private PropertyPlaceholder propertyPlaceholder;
	private ScspwsService verificaPort;

	@Inject
	public PlatinoSVDImpl(PropertyPlaceholder propertyPlaceholder) {
		
        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoVerificarDatosServiceImpl.class.getClassLoader().getResource("wsdl/svd.wsdl");
        verificaPort = new ScspwsService_Service(wsdlURL).getScspwsServicePort();

        WSUtils.configureEndPoint(verificaPort, getEndPoint());
        WSUtils.configureSecurityHeaders(verificaPort, propertyPlaceholder);
        PlatinoProxy.setProxy(verificaPort, propertyPlaceholder);
    }
	
	@Override
	public boolean isConfigured() {
		return hasConnection();
	}

	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de SVD ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de SVD ha sido inyectado con Platino y NO está operativo.");
	}


	@Override
	public void crearPeticion(PeticionSVDFAP peticion, List<SolicitudGenerica> solicitudes) {
		peticion.rellenarSolicitud(solicitudes);
	}

	@Override
	public Respuesta enviarPeticionSincrona(PeticionSVDFAP peticion) throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta enviarPeticionAsincrona(PeticionSVDFAP peticion) throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta solicitarRespuestaAsincrona(String idRespuesta) throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RespuestaPdf generarPDFRespuesta() throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta peticionRecover() throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
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
	
    private String getVersion() throws SvdException {
        return verificaPort.getVersion();
    }
    
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.svd.url");
	}
	
	private es.gobcan.platino.servicios.svd.peticionatributos.Atributos setAtributosPlatino (String codigoCertificado){
		es.gobcan.platino.servicios.svd.peticionatributos.Atributos atributos = new es.gobcan.platino.servicios.svd.peticionatributos.Atributos();
		atributos.setCodigoCertificado(codigoCertificado);
		
		return atributos;
	}
	
}
