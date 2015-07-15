package services.verificacionDatos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.inject.Inject;

import messages.Messages;
import models.Documento;
import models.ParametroSVDFAP;
import models.ParametrosServicioSVDFAP;
import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.itextpdf.text.Document;
import com.sun.java_cup.internal.runtime.Scanner;
import com.sun.java_cup.internal.runtime.Symbol;

import config.InjectorConfig;
import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.GestorDocumentalService;
import utils.AedUtils;
import utils.WSUtils;
import enumerado.fap.gen.TipoEstadoPeticionSVDFAPEnum;
import es.gobcan.platino.servicios.svd.ConfirmacionPeticion;
import es.gobcan.platino.servicios.svd.DatosTitularIdResi;
import es.gobcan.platino.servicios.svd.PeticionRecoverResponse;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import es.gobcan.platino.servicios.svd.ScspwsService;
import es.gobcan.platino.servicios.svd.ScspwsService_Service;
import es.gobcan.platino.servicios.svd.SvdException;
import es.gobcan.platino.servicios.svd.TransmisionDatos;
import es.gobcan.platino.servicios.svd.peticionpeticionasincrona.PeticionAsincrona;
import es.gobcan.platino.servicios.svd.peticionpeticionpdf.PeticionPdf;
import es.gobcan.platino.servicios.svd.peticionpeticionrecover.PeticionRecover;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;
import es.gobcan.platino.servicios.svd.respuestadatosespecificos.DatosEspecificos;
import es.gobcan.platino.servicios.svd.respuestadatosespecificosidresi.DatosEspecificosIdResi;
import es.gobcan.platino.servicios.svd.solicitudrespuestasolicitudrespuesta.SolicitudRespuesta;

@InjectSupport
public class PlatinoSVDServiceImpl implements SVDService {

	private PropertyPlaceholder propertyPlaceholder;
	private ScspwsService svdPort;
	private GestorDocumentalService gestorDocumentalService;

	private static final Logger log = Logger.getLogger(PlatinoSVDServiceImpl.class);

	@Inject
	public PlatinoSVDServiceImpl(PropertyPlaceholder propertyPlaceholder, GestorDocumentalService gestorDocumentalService) {

        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoSVDServiceImpl.class.getClassLoader().getResource("wsdl/svd.wsdl");
        svdPort = new ScspwsService_Service(wsdlURL).getScspwsServicePort();

        WSUtils.configureEndPoint(svdPort, getEndPoint());
        WSUtils.configureSecurityHeaders(svdPort, propertyPlaceholder);
        PlatinoProxy.setProxy(svdPort, propertyPlaceholder);
        
        this.gestorDocumentalService = gestorDocumentalService;
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

	/**
	 * Método que nos permite realizar una petición para la cesión de datos de forma síncrona.
	 * 
	 * Requesitos:
	 * - Debe existir la autorización para poder realizar la misma.
	 * - Sólo se permite una cesión de datos cada vez y tiene que haber al menos una.
	 */
	@Override
	public void peticionSincrona(PeticionSVDFAP peticion) throws SVDServiceException {

		try {
			PeticionSincrona peticionPlatino = SVDUtils.peticionSincronaFAPToPeticionSincronaPlatino(peticion);
			Respuesta respuestaPlatino = svdPort.peticionSincrona(peticionPlatino);
			SVDUtils.respuestaPlatinoToRespuestaFAP(respuestaPlatino, peticion);
			repercutirRespuestaExpedienteAED(peticion, peticion.solicitudesTransmision.get(0));			
		}
		catch (Exception e) {
			play.Logger.error("No se ha podido enviar la petición Síncrona. Causa: " + e.getMessage());
			throw new SVDServiceException("Error al realizar la petición síncrona", e);
		}

	}

	/**
	 * Método que nos permite realizar una petición para la cesión de datos de forma asíncrona.
	 * 
	 * Requesitos:
	 * - Debe existir la autorización para poder realizar la misma.
	 * - Se pueden enviar más de una petición de datos a la vez, el máximo lo define el servicio y como mínimo una.
	 */
	@Override
	public void peticionAsincrona(PeticionSVDFAP peticion) throws SVDServiceException {

		try {
			PeticionAsincrona peticionPlatino = SVDUtils.peticionAsincronaFAPToPeticionAsincronaPlatino(peticion);
			peticion.fechaPeticion = new DateTime().now();
			
			ConfirmacionPeticion confirmacionPeticion = svdPort.peticionAsincrona(peticionPlatino);
			
			//Atributos
			peticion.atributos.codigoCertificado = confirmacionPeticion.getAtributos().getCodigoCertificado();
			peticion.atributos.idPeticion = confirmacionPeticion.getAtributos().getIdPeticion();
			peticion.atributos.timestamp = confirmacionPeticion.getAtributos().getTimeStamp();
			peticion.atributos.numElementos = confirmacionPeticion.getAtributos().getNumElementos();

			//Estado
			peticion.atributos.estado.literalError = confirmacionPeticion.getAtributos().getEstado().getLiteralError();
		    peticion.fechaConfirmacion = SVDUtils.parseFecha(confirmacionPeticion.getAtributos().getTimeStamp());
			
			for (SolicitudTransmisionSVDFAP solicitudTransmision: peticion.solicitudesTransmision) {
				solicitudTransmision.fechaPeticion = peticion.fechaPeticion;
				solicitudTransmision.estado = TipoEstadoPeticionSVDFAPEnum.enviada.name();
			}
			
			peticion.estadoPeticion = TipoEstadoPeticionSVDFAPEnum.enviada.name();
		}
		catch (Exception e) {
			play.Logger.error("No se ha podido enviar la petición Asíncrona. Causa: " + e.getMessage());
			throw new SVDServiceException("Error al realizar la petición Asíncrona", e);
		}
	}

	/**
	 * Nos permite recuperar las respuestas a la petición de un número de cesiones de datos.
	 * 
	 * Requesitos:
	 * - Debe existir una petición con los datos del (uidUsuario, atributos...)
	 * - Para cada cesión de datos de la petición debe repercutirse el documento de la respuesta en 
	 *   los respectivos expedientes.
	 *   
	 */
	@Override
	public void solicitudRespuesta(PeticionSVDFAP peticion) throws SVDServiceException {

		try {
			SolicitudRespuesta solicitudRespuesta = new SolicitudRespuesta();
			solicitudRespuesta.setUidUsuario(peticion.getUidUsuario());
			solicitudRespuesta.setAtributos(SVDUtils.setAtributosSolicitudRespuestaPlatino(peticion));
			Respuesta respuesta = svdPort.solicitudRespuesta(solicitudRespuesta);
			SVDUtils.respuestaPlatinoToRespuestaFAP(respuesta, peticion);
			peticion.estadoPeticion = TipoEstadoPeticionSVDFAPEnum.recibida.name();
			for (SolicitudTransmisionSVDFAP solicitudTransmision: peticion.solicitudesTransmision) {
				solicitudTransmision.fechaRespuesta = solicitudTransmision.datosGenericos.transmision.fechaGeneracion;
				solicitudTransmision.estado = TipoEstadoPeticionSVDFAPEnum.recibida.name();
				repercutirRespuestaExpedienteAED(peticion, solicitudTransmision);	
			}
		} catch (Exception e) {
			play.Logger.error("No se ha podido solicitar la respuesta: " + e.getMessage());
			throw new SVDServiceException("Error al realizar la solicitud de respuesta", e);
		}

	}

	/**
	 * Método que recupera un fichero con la respuesta a la petición de cesión de datos.
	 * 
	 * Requisitos:
	 * - Debe existir una petición con los datos del (uidUsuario, idPeticion, idTransmision)
	 * - Se recibe un documneto por cada idTransmision
	 */
	@Override
	public RespuestaPdf peticionPDF(String uidUsuario, String idPeticion, String idTransmision) throws SVDServiceException {
		PeticionPdf peticionPDF = new PeticionPdf();
		RespuestaPdf respuestaPdf = new RespuestaPdf();
		try {
			peticionPDF.setUidUsuario(uidUsuario);
			peticionPDF.setIdPeticion(idPeticion);
			peticionPDF.setIdTransmision(idTransmision);
			
			respuestaPdf = svdPort.peticionPdf(peticionPDF);
		} catch (Exception e) {
			play.Logger.error("Se ha producido un error recuperando el PDF, causa: " + respuestaPdf.getError() +", excepción: " + e.getMessage());
			throw new SVDServiceException("Se ha producido un error recuperando el PDF", e);
		}
		return respuestaPdf;
	}

	/**
	 * Método que permite recuperar una petición de cesión de datos.
	 * 
	 * Requisitos:
	 * - Debe existir una petición con los datos del (uidUsuario, idPeticion)
	 * 
	 */
	@Override
	public Respuesta peticionRecover(PeticionSVDFAP peticion) throws SVDServiceException {
		Respuesta peticionResponse = null;
		try {
			PeticionRecover peticionRecover = new PeticionRecover();
			peticionRecover.setIdPeticion(peticion.atributos.idPeticion);
			peticionRecover.setUidUsuario(peticion.uidUsuario);
			peticionResponse = svdPort.peticionRecover(peticionRecover);
		} catch(Exception e){
			play.Logger.error("Se ha producido un error recuperando la peticion" + e.getMessage());
			throw new SVDServiceException("Se ha producido un error recuperando la peticion", e);
		}
		return peticionResponse;
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
	
	/**
	 * Método que se encarga de recuperar el justificante de la cesión de datos y repercutirlo en el expediente.
	 * @param peticion
	 * @param solicitud
	 * @throws SVDServiceException
	 */
	private void repercutirRespuestaExpedienteAED(PeticionSVDFAP peticion,SolicitudTransmisionSVDFAP solicitud) throws SVDServiceException{
		try {
			   RespuestaPdf respuestaPDFSVD = peticionPDF(peticion.uidUsuario, peticion.atributos.idPeticion, solicitud.datosGenericos.transmision.idTransmision);
			  
			   //Creando y almacenando el justificante de la cesión de datos en el AED
			   DataHandler docPDF = respuestaPDFSVD.getPdf();
			   solicitud.justificanteSVD = new Documento();
			   solicitud.justificanteSVD.descripcion = "Justificante de peticion para la cesión de datos de " + peticion.nombreServicio;
			   solicitud.justificanteSVD.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteSVD");
			   gestorDocumentalService.saveDocumentoTemporal(solicitud.justificanteSVD, docPDF.getInputStream(), "JustificanteSVD.pdf");
			   
			   //Clasificando el justificante de la sesión de datos en el expediente
			   List<Documento> lstdocDocumentos = new ArrayList<Documento>();
			   lstdocDocumentos.add(solicitud.justificanteSVD);
			   gestorDocumentalService.clasificarDocumentos(solicitud.solicitud, lstdocDocumentos);
		}catch(Exception e){
			throw new SVDServiceException("No se ha podido obtener el justificante de la petición para la cesión de datos con código: " + peticion.atributos.idPeticion, e);
		}
	}
	
}
