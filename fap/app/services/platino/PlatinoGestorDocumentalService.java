package services.platino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.MTOMFeature;

import messages.Messages;
import models.ExpedientePlatino;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import config.InjectorConfig;

import platino.DatosDocumento;
import platino.DatosFirmante;
import platino.DatosRegistro;
import platino.PlatinoProxy;
import play.Play;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import utils.BinaryResponse;
import utils.WSUtils;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.registro.Documento;
import es.gobcan.platino.servicios.registro.Documentos;
import es.gobcan.platino.servicios.sgrde.DocumentoBase;
import es.gobcan.platino.servicios.sgrde.DocumentoExpediente;
import es.gobcan.platino.servicios.sgrde.ElementoNoEncontradoException;
import es.gobcan.platino.servicios.sgrde.ErrorInternoException;
import es.gobcan.platino.servicios.sgrde.Expediente;
import es.gobcan.platino.servicios.sgrde.FirmasElectronicas;
import es.gobcan.platino.servicios.sgrde.InformacionFirmaElectronica;
import es.gobcan.platino.servicios.sgrde.MetaInformacionException;
import es.gobcan.platino.servicios.sgrde.SGRDEServicePortType;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;
import es.gobcan.platino.servicios.sgrde.UsuarioNoValidoException;

import play.mvc.results.*;

/**
 * GestorDocumentalServiceImpl
 */
public class PlatinoGestorDocumentalService {
    private static Logger log = Logger.getLogger(PlatinoGestorDocumentalService.class);

    private final PropertyPlaceholder propertyPlaceholder;
    private volatile static SGRDEServicePortType gestorDocumentalPort;

    public PlatinoGestorDocumentalService(PropertyPlaceholder propertyPlaceholder) {
        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoGestorDocumentalService.class.getClassLoader().getResource("wsdl/sgrde.wsdl");
        gestorDocumentalPort = new SGRDEServiceProxy(wsdlURL).getSGRDEServiceProxyPort(new MTOMFeature());

        WSUtils.configureEndPoint(gestorDocumentalPort, getEndPoint());
        WSUtils.configureSecurityHeaders(gestorDocumentalPort, propertyPlaceholder);
        PlatinoProxy.setProxy(gestorDocumentalPort, propertyPlaceholder);  
    }

    public boolean hasConnection() {
        boolean result = false;
        try {
            result = getVersion() != null;
        } catch (Exception e) {
            log.error("El servicio web del gestor documental no tiene conexion con " + getEndPoint());
        }
        return result;
    }

    private String getEndPoint() {
        return propertyPlaceholder.get("fap.platino.gestordocumental.url");
    }

    private String getVersion() {
        return gestorDocumentalPort.getVersion();
    }
    
    protected SGRDEServicePortType getGestorDocumentalPort(){
    	return this.gestorDocumentalPort;
    }

    /**
     * Crea un expediente en el Gestor Documental de Platino
     */
    public void crearExpediente(ExpedientePlatino exp) throws PlatinoGestorDocumentalServiceException {
        log.info("CrearExpediente Platino -> IN");

        Expediente expediente = new Expediente();
        expediente.setFechaApertura(WSUtils.getXmlGregorianCalendar(exp.getFechaApertura()));
        expediente.setNumeroExp(exp.getNumero());

        String descripcion = propertyPlaceholder.get("fap.platino.gestordocumental.expediente.descripcion");
        expediente.setDescExp(descripcion);

        try {
            String uri = gestorDocumentalPort.crearExpediente(exp.getRuta(), expediente);
            exp.uri = uri;
            exp.creado = true;
            exp.save();

            log.info("Expediente creado " + exp.uri);
            log.info("CrearExpediente -> EXIT OK");
        } catch (Exception e) {
            String mensajeError = " [Expediente " + exp.getNumero() + "]: ERROR al crear el expediente en Platino";
            if (e instanceof ErrorInternoException) {
                mensajeError += ": " + ((ErrorInternoException) e).getFaultInfo().getMessage();
            }
            log.error("CrearExpediente -> EXIT ERROR " + mensajeError);
            throw new PlatinoGestorDocumentalServiceException(mensajeError, e);
        }
    }

    /**
     * Crea un documento en el Gestor Documental de Platino
     * @throws GestorDocumentalServiceException 
     */
    public static String guardarDocumento(String expedientePlatinoRuta, DatosDocumento documentoRegistrar)
            throws PlatinoGestorDocumentalServiceException {
        
        try {
            // Metainformación
            DocumentoExpediente documentoExpediente = new DocumentoExpediente();
            documentoExpediente.setFechaDoc(documentoRegistrar.getFecha());
            documentoExpediente.setTipoDoc(documentoRegistrar.getTipoDoc());
            documentoExpediente.setDescDoc(documentoRegistrar.getDescripcion());
            documentoExpediente.setTipoMime(documentoRegistrar.getTipoMime());
            documentoExpediente.setAdmiteVersionado(documentoRegistrar.isAdmiteVersionado());

            if(documentoRegistrar.getFirmantes() != null){
                // Firma normal o paralela
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
            }

            // Ruta
            UUID uuidDocumento = UUID.randomUUID();
            String ruta = expedientePlatinoRuta + "/" + uuidDocumento;

            // Insertar documento en el Gestor Documental
            DataHandler dataHandler = new DataHandler(documentoRegistrar.getContenido());
            log.info("Inserta documento en el gestor documental");
            String urn = gestorDocumentalPort.insertarDocumento(dataHandler, ruta, documentoExpediente); // Metainformación
            log.info("Documento insertado");

            return urn;
        } catch (Exception e) {
            String mensaje = "Error al guardar el documento en el Gestor Documental";
            if (e instanceof ErrorInternoException) {
                mensaje += ". ErrorInternoException: " + ((ErrorInternoException) e).getFaultInfo().getMessage();
            }
            if (e instanceof MetaInformacionException) {
                MetaInformacionException mie = (MetaInformacionException) e;
                mensaje += mie.getFaultInfo().getMessage();
            }

            play.Logger.error(mensaje);
            throw new PlatinoGestorDocumentalServiceException(mensaje, e);
        }
    }
    
    public static String convertToHexNoQuery (String uriExpediente){
        String uriHex = "";
        //Se recorre toda la uri buscando números despues de / y se pasa a hex UNICAMENTE el primer dígito
         Pattern pattern = Pattern.compile ("([^/])+"); //Hace grupos entre /Grupo/
         Matcher matcher = pattern.matcher(uriExpediente); 
         while (matcher.find()){
           String subCadena = uriExpediente.substring(matcher.start(), matcher.end());
           String primerCaracter = subCadena.substring(0, 1); 
             if(isNumeric(primerCaracter)){ //Si el primer caracter es numerico lo paso a hex             
               String aux = Integer.toHexString(primerCaracter.charAt(0));
               String hex = "_x"+String.format("%1$4s", aux).replace(' ', '0')+"_";
               uriHex += hex + uriExpediente.substring(matcher.start()+1, matcher.end())+"/";
             } else {
               uriHex += subCadena+"/";
             }           
         }
        return uriHex;
      }
    

    public String convertToHex (String uriExpediente){
       return "Ruta: \"" + convertToHexNoQuery(uriExpediente) +"*\"" ;
     }
    
    private static boolean isNumeric(String cadena){
    	try {
    		Integer.parseInt(cadena);
    		return true;
    	} catch (NumberFormatException nfe){
    		return false;
    	}
    }
    
	
	/**
	 * 
	 * @param uriDocumento URI del documento que se sube al gestor documental de Platino si no está en éste.
	 * @param uidUsuario Identificador único del funcionario en el ldap del gobierno. El identificador sepuede pasar en minúsculas o mayúsculas.
	 * @param service Servicio desde el cual se llama este método
	 */
	//TODO Este método debe ir en la implementación del GestorDocumental de Platino
	public static String obtenerURIPlatino(String uriDocumento, Object service) {
		GestorDocumentalService gestorDocumentalPort = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		
		//Caso en el que el documento se encuentra en el AED de la ACIISI
		models.Documento documento = models.Documento.findByUri(uriDocumento); // Documento subido al gestor documental de la ACIISI
		
		try {
			//Subir documento a firmar a gestor documental de platino (si no está subido)
			if ((documento != null) && (documento.uriPlatino == null)) { // El documento está en el Gestor Documental de la ACIISI y no está en Platino
				
				//Obtenemos la ruta del expediente (convertida a platino)
				ExpedientePlatino expedientePlatino = ExpedientePlatino.all().first();
				String uriPlatinoExpediente = convertToHexNoQuery(expedientePlatino.getRuta());
				
				//Obtenemos el documento original del gestor documental
				BinaryResponse doc = gestorDocumentalPort.getDocumentoByUri(documento.uri);
				
				//Configuramos los datos de subida del documento
				DatosDocumento datos = new DatosDocumento();
				datos.setContenido(doc.contenido.getDataSource());
				datos.setTipoMime(doc.contenido.getContentType());
				datos.setFecha(PlatinoPortafirmaServiceImpl.DateTime2XMLGregorianCalendar(DateTime.now())); // TODO: Cambiar el modo de conversion
				datos.setDescripcion(documento.descripcionVisible);
				datos.setAdmiteVersionado(true);
				
				//Subimos el documento al gestor documental de platino
				documento.uriPlatino = guardarDocumento(uriPlatinoExpediente, datos);
				documento.save();
				
				return documento.uriPlatino;
			}
			else if ((documento != null) && (documento.uriPlatino != null)) {  // El documento está en el Gestor Documental de la ACIISI y está en Platino
				return documento.uriPlatino;
			}
			else { //  El documento no está en el Gestor Documental de la ACIISI
				models.Documento documentoPlatino = models.Documento.findByUriPlatino(uriDocumento);
				if (documentoPlatino != null)
					return documentoPlatino.uriPlatino;
				else {
					play.Logger.error("El documento con la uri "+uriDocumento+" no existe.");
					return null;
				}
			}
		} catch (PlatinoGestorDocumentalServiceException e) {
			e.printStackTrace();
			play.Logger.error("Error al acceder al gestor documental de platino: " + e.getMessage());
			//throw new SolicitudFirmaExcepcion("Error al acceder al gestor documental de platino: " + e.getMessage(), e);
		} catch (GestorDocumentalServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;	
	}
	
	/**
	 * Método para obtener la firma electrónica de un documento firmado desde el AED de Platino
	 * @param uriDocumento URI del documento que se sube al gestor documental de Platino
	 * @return documento firmado según la clase Models.Documento
	 */
	public FirmasElectronicas obtenerFirmaDocumento(String uriDocumento){
		String firma = null;
		FirmasElectronicas firmasElectronicas = null;
		play.Logger.info("Obtener documento.");
		try {
			// Obtenemos la firma electronica del documento que buscamos el aed de Platino
			firmasElectronicas = gestorDocumentalPort.obtenerMetaDoc(uriDocumento).getFirmasElectronicas();
			firma = firmasElectronicas.getFirma();
			System.out.println("Firma = "+ firma);
		} catch (ElementoNoEncontradoException e) {
			play.Logger.error("El documento con la uri "+uriDocumento+" no existe.");
		} catch (UsuarioNoValidoException e) {
			play.Logger.error("Usuario no válido");
		} catch (ErrorInternoException e) {
			play.Logger.error("Se ha producido un error en el servicio");
		}

		return firmasElectronicas;
	}
}
