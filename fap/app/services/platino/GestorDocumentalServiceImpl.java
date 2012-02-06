package services.platino;

import java.net.URL;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.MTOMFeature;

import models.ExpedientePlatino;

import org.apache.log4j.Logger;

import platino.DatosDocumento;
import platino.DatosFirmante;
import platino.DatosRegistro;
import platino.PlatinoProxy;
import properties.PropertyPlaceholder;
import utils.WSUtils;
import es.gobcan.platino.servicios.registro.Documento;
import es.gobcan.platino.servicios.registro.Documentos;
import es.gobcan.platino.servicios.sgrde.DocumentoBase;
import es.gobcan.platino.servicios.sgrde.DocumentoExpediente;
import es.gobcan.platino.servicios.sgrde.ErrorInternoException;
import es.gobcan.platino.servicios.sgrde.Expediente;
import es.gobcan.platino.servicios.sgrde.FirmasElectronicas;
import es.gobcan.platino.servicios.sgrde.InformacionFirmaElectronica;
import es.gobcan.platino.servicios.sgrde.MetaInformacionException;
import es.gobcan.platino.servicios.sgrde.SGRDEServicePortType;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;

/**
 * GestorDocumentalServiceImpl
 * 
 * El servicio esta preparado para inicializarse de forma lazy.
 * Por lo tanto siempre que se vaya a consumir el servicio web
 * se deberia acceder a "getGestorDocumentalPort" en lugar de acceder directamente
 * a la property
 * 
 */
public class GestorDocumentalServiceImpl implements GestorDocumentalPlatinoService {
	private static Logger log = Logger.getLogger(GestorDocumentalPlatinoService.class);
	
	private String endPoint;
	private PropertyPlaceholder propertyPlaceholder;
	private volatile SGRDEServicePortType gestorDocumentalPort;
	
	public GestorDocumentalServiceImpl(PropertyPlaceholder propertyPlaceholder){
		init(propertyPlaceholder);
	}

	public GestorDocumentalServiceImpl(PropertyPlaceholder propertyPlaceholder, boolean eagerInitialization){
		init(propertyPlaceholder);
		if(eagerInitialization)
			getGestorDocumentalPort();
	}
	
	private void init(PropertyPlaceholder propertyPlaceholder){
		if(propertyPlaceholder == null)
			throw new NullPointerException();
		this.propertyPlaceholder = propertyPlaceholder;
		endPoint = propertyPlaceholder.get("fap.platino.gestordocumental.url");
		if(endPoint == null)
			throw new IllegalStateException();		
	}
	
	private SGRDEServicePortType getGestorDocumentalPort(){
		//Double-check idiom for lazy initialization
		SGRDEServicePortType result = gestorDocumentalPort;
		if(result == null){
			synchronized(this){
				result = gestorDocumentalPort;
				if(result == null){
					URL wsdlURL = GestorDocumentalServiceImpl.class.getClassLoader().getResource("wsdl/sgrde.wsdl");
					result = gestorDocumentalPort = new SGRDEServiceProxy(wsdlURL).getSGRDEServiceProxyPort(new MTOMFeature());
					
					WSUtils.configureEndPoint(gestorDocumentalPort, endPoint);
					WSUtils.configureSecurityHeaders(gestorDocumentalPort, propertyPlaceholder);
					PlatinoProxy.setProxy(gestorDocumentalPort, propertyPlaceholder);					
				}
			}
		}
		return result;
	}
	
	

	public boolean hasConnection() {
		boolean result = false;
		try {
			result = getVersion() != null;
		}catch(Exception e){
			log.error("El servicio web del gestor documental no tiene conexion con " + endPoint );
		}
		return result;
	}

	public String getEndPoint() {
		return endPoint;
	}

	
	public String getVersion(){
		return getGestorDocumentalPort().getVersion();
	}
	
	/**
	 * Crea un expediente en el Gestor Documental de Platino
	 */
	public void crearExpediente(ExpedientePlatino exp) throws Exception {
		log.info("CrearExpediente Platino -> IN");
		
		Expediente expediente = new Expediente();
		
		XMLGregorianCalendar fechaApertura = DatatypeFactory.newInstance().newXMLGregorianCalendar(exp.getFechaApertura().toGregorianCalendar());
		expediente.setFechaApertura(fechaApertura);
		
		expediente.setNumeroExp(exp.getNumero());
		
		String descripcion = propertyPlaceholder.get("fap.platino.gestordocumental.expediente.descripcion");
		expediente.setDescExp(descripcion);
		
		try {
			String uri = getGestorDocumentalPort().crearExpediente(exp.getRuta(), expediente);
			exp.uri = uri;
			exp.creado = true;
			exp.save();
			
			log.info("Expediente creado "+ exp.uri);
			log.info("CrearExpediente -> EXIT OK");
		} catch (Exception e) {
			String mensajeError = " [Expediente " + exp.getNumero() + "]: ERROR al crear el expediente en Platino"; 
			if (e instanceof ErrorInternoException) {
				mensajeError += ": " + ((ErrorInternoException) e).getFaultInfo().getMessage();
			}
			log.error("CrearExpediente -> EXIT ERROR "+ mensajeError);
			throw e;
		}
	}
	
	/**
	 * Crea un documento en el Gestor Documental de Platino
	 */
	public DocumentoExpediente guardarDocumento(String expedientePlatinoRuta,DatosDocumento documentoRegistrar) throws Exception {
		try {
		    // Metainformación  
		    DocumentoExpediente documentoExpediente = new DocumentoExpediente();
		    documentoExpediente.setFechaDoc(documentoRegistrar.getFecha());
		    documentoExpediente.setTipoDoc(documentoRegistrar.getTipoDoc());
		    documentoExpediente.setDescDoc(documentoRegistrar.getDescripcion());
		    documentoExpediente.setTipoMime(documentoRegistrar.getTipoMime());
		    documentoExpediente.setAdmiteVersionado(documentoRegistrar.isAdmiteVersionado());

		    
		   // if(documentoRegistrar.getFirmantes() != null){
			    //Firma normal o paralela
			    FirmasElectronicas firmasElec = new FirmasElectronicas();
			    firmasElec.setFirma(documentoRegistrar.getFirmaXml());
			    
			    // Copiamos firmantes
			    for (DatosFirmante firmante : documentoRegistrar.getFirmantes()) {
			    	InformacionFirmaElectronica infoFirma = new InformacionFirmaElectronica();
			    	infoFirma.setDescFirmante(firmante.getDescFirmante());
			    	infoFirma.setFechaFirma(firmante.getFechaFirma());
			    	infoFirma.setIdFirmante(firmante.getIdFirmante());
			    	infoFirma.setCargoFirmante(firmante.getCargoFirmante());
			    	infoFirma.setURIFirmante(firmante.getUriFirmante());
			    	
			    	firmasElec.getInformacionFirmaElectronica().add(infoFirma);
			    }
			    documentoExpediente.setFirmasElectronicas(firmasElec);
		    //}
		    
		    // Ruta
		    UUID uuidDocumento = UUID.randomUUID();	
		    String ruta = expedientePlatinoRuta + "/" + uuidDocumento;
		    
		    DocumentoBase docBase;
		    // Insertar documento en el Gestor Documental
		    DataHandler dataHandler = new DataHandler(documentoRegistrar.getContenido());
		    log.info("Inserta documento en el gestor documental");
			String urn = getGestorDocumentalPort().insertarDocumento(dataHandler, ruta, documentoExpediente); // Metainformación
			log.info("Documento insertado");
			
			documentoExpediente.setURI(urn);
			documentoRegistrar.setUriPlatino(urn);


			return documentoExpediente;
	    } catch (Exception e) {
	    	String mensaje = "Error al guardar el documento en el Gestor Documental";
	    	if (e instanceof ErrorInternoException) {
	    		mensaje += ". ErrorInternoException: " + ((ErrorInternoException) e).getFaultInfo().getMessage();
	    	}
	    	if(e instanceof MetaInformacionException){
	    		MetaInformacionException mie = (MetaInformacionException)e;
	    		mensaje += mie.getFaultInfo().getMessage();
	    	}
	    	
	    	play.Logger.error(mensaje);
	    	throw e;
	    }
	}
	
	/**
	 * Devuelve la lista de documentos preparados para registrar
	 */
	public Documentos guardarSolicitudEnGestorDocumental(String expedienteGestorDocumentalRuta, DatosDocumento documentoRegistrar) throws Exception {
		log.info("GuardarSolicitudEnGestorDocumental -> IN");
		Documentos documentosGestorDocumental = new Documentos();
		// 2A) Insertar Documento de la Solicitud en el Gestor Documental (en el expediente creado)
		if (documentoRegistrar.getUriPlatino() == null) {
			// Hay que guardarlo en el Gestor Documental
			log.info("El documento no está creado");
			DocumentoExpediente documentoExpedientePlatino = guardarDocumento(expedienteGestorDocumentalRuta, documentoRegistrar);
			documentoRegistrar.setUriPlatino(documentoExpedientePlatino.getURI());
			log.info("Documento creado con URI "+documentoRegistrar.getUriPlatino());
		} else {
			// Ya había sido guardado anteriormente (hubo algún error posterior)
		}
		Documento doc = DatosRegistro.documentoSGRDEToRegistro(documentoRegistrar.getContenido(), documentoRegistrar.getUriPlatino());
		documentosGestorDocumental.getDocumento().add(doc);
		log.info("GuardarSolicitudEnGestorDocumental -> EXIT OK");
		return documentosGestorDocumental;
	}
	
}
