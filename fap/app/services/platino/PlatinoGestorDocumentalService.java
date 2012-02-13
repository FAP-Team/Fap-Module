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
import services.GestorDocumentalServiceException;
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
 */
public class PlatinoGestorDocumentalService {
    private static Logger log = Logger.getLogger(PlatinoGestorDocumentalService.class);

    private final PropertyPlaceholder propertyPlaceholder;
    private volatile SGRDEServicePortType gestorDocumentalPort;

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
    public String guardarDocumento(String expedientePlatinoRuta, DatosDocumento documentoRegistrar)
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

            DocumentoBase docBase;
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
}
