package services.platino;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.ParametroSVD;
import models.ParametrosServicio;
import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;

import org.apache.log4j.Logger;

import platino.PlatinoProxy;
import properties.PropertyPlaceholder;
import services.SVDService;
import services.SVDServiceException;
import utils.SVDUtils;
import utils.WSUtils;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import es.gobcan.platino.servicios.svd.ScspwsService;
import es.gobcan.platino.servicios.svd.ScspwsService_Service;
import es.gobcan.platino.servicios.svd.SvdException;
import es.gobcan.platino.servicios.svd.peticionpeticionpdf.PeticionPdf;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;

public class PlatinoSVDServiceImpl implements SVDService {

	private PropertyPlaceholder propertyPlaceholder;
	private ScspwsService svdPort;

	private static final Logger log = Logger.getLogger(PlatinoSVDServiceImpl.class);

	@Inject
	public PlatinoSVDServiceImpl(PropertyPlaceholder propertyPlaceholder) {

        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoVerificarDatosServiceImpl.class.getClassLoader().getResource("wsdl/svd.wsdl");
        svdPort = new ScspwsService_Service(wsdlURL).getScspwsServicePort();

        WSUtils.configureEndPoint(svdPort, getEndPoint());
        WSUtils.configureSecurityHeaders(svdPort, propertyPlaceholder);
        PlatinoProxy.setProxy(svdPort, propertyPlaceholder);
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
	public void crearPeticion(PeticionSVDFAP peticion, List<SolicitudTransmisionSVDFAP> solicitudes, String tipoServicio) {

		//peticion.uidUsuario = "dgonmor";

		peticion.uidUsuario = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "uidUsuario").first();
		peticion.nifFuncionario = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "nifFuncionario").first();

		peticion.solicitudesTransmision = solicitudes;

		peticion.atributos.codigoCertificado = ParametrosServicio.find("select codigoCertificado from ParametrosServicio parametrosServicio where nombreServicio=?", tipoServicio).first();
		peticion.nombreServicio = tipoServicio;
		peticion.estadoPeticion = "creada";

		peticion.save();

		play.Logger.info("Se ha creado la petición SVD correctamente");
	}

	@Override
	public Respuesta enviarPeticionSincrona(PeticionSVDFAP peticion) throws SVDServiceException {

		try {
			PeticionSincrona peticionPlatino = SVDUtils.peticionSincronaFAPToPeticionSincronaPlatino(peticion);
			return svdPort.peticionSincrona(peticionPlatino);
		}
		catch (Exception e) {
			System.out.println("No se ha podido enviar la petición Síncrona. Causa: " + e);
			throw new SVDServiceException("Error al realizar la petición síncrona");
		}

	}

	@Override
	public Respuesta enviarPeticionAsincrona(PeticionSVDFAP peticion) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta solicitarRespuestaAsincrona(String idRespuesta) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RespuestaPdf peticionPDF(String uidUsuario, String idPeticion, String idTransmision) throws SVDServiceException {

		PeticionPdf peticionPDF = new PeticionPdf();

		peticionPDF.setUidUsuario(uidUsuario);
		peticionPDF.setIdPeticion(idPeticion);
		peticionPDF.setIdTransmision(idTransmision);

		try {
			return svdPort.peticionPdf(peticionPDF);
		} catch (SvdException e) {
			Messages.error("Se ha producido un error recuperando el PDF");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Respuesta peticionRecover() throws SVDServiceException {
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
        return svdPort.getVersion();
    }

	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.svd.url");
	}

}
