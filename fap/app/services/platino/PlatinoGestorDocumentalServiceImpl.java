package services.platino;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import platino.DatosDocumento;
import platino.DatosFirmante;
import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.eadmon.procedimientos.ws.Procedimientos;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosInterface;
import es.gobcan.platino.servicios.sgrde.DocumentoBase;
import es.gobcan.platino.servicios.sgrde.DocumentoExpediente;
import es.gobcan.platino.servicios.sgrde.ErrorInternoException;
import es.gobcan.platino.servicios.sgrde.Expediente;
import es.gobcan.platino.servicios.sgrde.FirmasElectronicas;
import es.gobcan.platino.servicios.sgrde.InformacionFirmaElectronica;
import es.gobcan.platino.servicios.sgrde.MetaInformacionException;
import es.gobcan.platino.servicios.sgrde.SGRDEServicePortType;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;

import models.Agente;
import models.Documento;
import models.ExpedienteAed;
import models.ExpedientePlatino;
import models.Firma;
import models.InformacionRegistro;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import models.Tramite;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.filesystem.TipoDocumentoEnTramite;
import services.filesystem.TipoDocumentoGestorDocumental;
import utils.BinaryResponse;
import utils.WSUtils;
import org.apache.commons.codec.binary.Hex;

import controllers.fap.AgenteController;

import es.gobcan.platino.servicios.procedimientos.DBProcedimientosException_Exception;
import es.gobcan.platino.servicios.procedimientos.DBProcedimientosServiceBean;
import es.gobcan.platino.servicios.procedimientos.DBProcedimientosServiceBeanService;
import es.gobcan.platino.servicios.procedimientos.ProcedimientoWSItem;

@InjectSupport
public class PlatinoGestorDocumentalServiceImpl implements GestorDocumentalService {

	 private static Logger log = Logger.getLogger(PlatinoGestorDocumentalService.class);

	    private PropertyPlaceholder propertyPlaceholder;
	    private SGRDEServicePortType gestorDocumentalPort;
        //private final DBProcedimientosServiceBean servicioProcedimientosPort;	    

	    @Inject
	    public PlatinoGestorDocumentalServiceImpl(PropertyPlaceholder propertyPlaceholder) {
	        this.propertyPlaceholder = propertyPlaceholder;

	        URL wsdlURL = PlatinoGestorDocumentalServiceImpl.class.getClassLoader().getResource("wsdl/sgrde.wsdl");
	        gestorDocumentalPort = new SGRDEServiceProxy(wsdlURL).getSGRDEServiceProxyPort(new MTOMFeature());

//	        DBProcedimientosServiceBeanService servicioProcedimientos = new DBProcedimientosServiceBeanService();
//	        servicioProcedimientosPort = servicioProcedimientos.getDBProcedimientosServiceBeanPort();
	        
	        WSUtils.configureEndPoint(gestorDocumentalPort, getEndPoint());
	        WSUtils.configureSecurityHeaders(gestorDocumentalPort, propertyPlaceholder);
	        PlatinoProxy.setProxy(gestorDocumentalPort, propertyPlaceholder);
	        
	        Client client = ClientProxy.getClient(gestorDocumentalPort);
			HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
			httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
			httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
			httpConduit.setClient(httpClientPolicy);			
	    }


	// Configura el gestorDocumental
	@Override
	public void configure() throws GestorDocumentalServiceException {
//		//Comprobar que existe el procedimiento -> Configurado el GestorDoc
//		 try {
//			 ProcedimientoWSItem respuesta = servicioProcedimientosPort.consultarProcedimiento(FapProperties.get("fap.aed.procedimientos.procedimiento.uri"), null);
//		} catch (DBProcedimientosException_Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			log.error("El gestor documental de Platino no está configurado correctamente, asegúrese de que existe el procedimiento");
//		}
//		
	}

	@Override
	public boolean isConfigured() {
		if (hasConnection())
			return true;
		return false;
	}
	
	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de GestorDocumental ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de GestorDocumental ha sido inyectado con Platino y NO está operativo.");
    }
	

	@Override
	public String crearExpediente(SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
		 log.info("CrearExpediente Platino -> IN");
		 	ExpedientePlatino exp = solicitud.expedientePlatino;
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
	            throw new GestorDocumentalServiceException(mensajeError, e);
	        }
	        return exp.numero;
	}

	@Override
	public List<String> getDocumentosEnExpediente(String expediente) throws GestorDocumentalServiceException {
		List<String>  uris = new ArrayList<String>();
		try {
			// OJO Número máximo de documentos
			expediente = convertoToHex(expediente);
			uris = gestorDocumentalPort.buscarDocumentos(expediente, 90);
		} catch (ErrorInternoException e) {
			System.out.println("Error obteniendo los documentos del expediente: "+expediente+"\n Traza error:"+e.getMessage());
			e.printStackTrace();
		}
        return uris;
	}

	@Override
	public List<Documento> getDocumentosPorTipo(String tipoDocumento) throws GestorDocumentalServiceException {
		if (tipoDocumento == null || tipoDocumento.isEmpty())
			return Collections.emptyList();
    	
			Agente agente = AgenteController.getAgente();
			// 1) Añadiendo el procedimiento a la consulta
			String consulta = propertyPlaceholder.get("fap."+propertyPlaceholder.get("fap.defaultAED")+".procedimiento");
			// Conjunto de documentos donde su tipo es 'tipoDocumento' y el interesado es el usuario logueado
			List<String> listaDocsUris;
			List<PropiedadesDocumento> listaDocs;
			List<models.Documento> listaDocumentos = new ArrayList<models.Documento>();
			List<models.Documento> listaDocumentosUris = new ArrayList<models.Documento>();
			try {
				//Buscar por procedimiento, tipoDoc e interesado 
				// 2) Añadiendo el interesado a la consulta
				
				// 3) Añadiendo el tipo de documento a la consulta
				
				// Obtengo lista de uris que cumplen las tres condiciones
				listaDocsUris = gestorDocumentalPort.buscarDocumentos(consulta, 90);
				if(listaDocsUris.isEmpty())
		    		return Collections.emptyList();
		
		    	//Obtener las propiedades de los documentos que nos ha devuelto la búsqueda
				// DESCOMENTAR
//		    	for (PropiedadesDocumento propiedadesDoc : listaDocs) {
//		    		models.Documento doc = new models.Documento();
//		    		propiedadesDoc.getIdentificador();
//		    		doc.docAed2Doc(propiedadesDoc, tipoDocumento);
//		    		listaDocumentos.add(doc);
//		    		doc.delete();
//		    	}
			} catch (ErrorInternoException e) {
				throw new GestorDocumentalServiceException("Error extrayendo los documentos por tipo");
				//e.printStackTrace();
			}
			    	return listaDocumentos;
			
	}

	@Override
	public BinaryResponse getDocumento(Documento documento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryResponse getDocumentoConInformeDeFirma(Documento documento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentoFirmaByUri(String uriDocumento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String saveDocumentoTemporal(Documento documento,
			InputStream inputStream, String filename)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String saveDocumentoTemporal(Documento documento, File file)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateDocumento(Documento documento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteDocumento(Documento documento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clasificarDocumentos(SolicitudGenerica solicitud,
			List<Documento> documentos,
			InformacionRegistro informacionRegistro, boolean notificable)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clasificarDocumentos(SolicitudGenerica solicitud,
			List<Documento> documentos, InformacionRegistro informacionRegistro)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clasificarDocumentos(SolicitudGenerica solicitud,
			List<Documento> documentos) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clasificarDocumentos(SolicitudGenerica solicitud,
			List<Documento> documentos, boolean notificable)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clasificarDocumentoResolucion(ResolucionFAP resolucionFap)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clasificarDocumentosConsulta(ResolucionFAP resolucionFap)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String crearExpedienteConvocatoria()
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void agregarFirma(Documento documento, Firma firma)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void agregarFirma(Documento documento, String firmaStr)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Firma getFirma(Documento documento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Tramite> getTramites() throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void actualizarCodigosExclusion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<TipoDocumentoEnTramite> getTiposDocumentosAportadosCiudadano(Tramite tramite) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExpReg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TipoDocumentoGestorDocumental> getListTiposDocumentosAportadosCiudadano(Tramite tramite) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String crearExpediente(ExpedienteAed expedienteAed)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String modificarInteresados(ExpedienteAed expedienteAed, SolicitudGenerica solicitud)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryResponse getDocumentoByUri(String uriDocumento) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryResponse getDocumentoConInformeDeFirmaByUri(String uriDocumento) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void duplicarDocumentoSubido(String uriDocumento) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, Documento dbDocumento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void duplicarDocumentoSubido(String uriDocumento, SolicitudGenerica solicitud)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, Documento dbDocumento, SolicitudGenerica solicitud) 
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copiarDocumentoEnExpediente(String uri, List<ExpedienteAed> expedientesAed) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copiarListaDocumentoEnExpediente(List<String> uri, List<ExpedienteAed> expedientesAed) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean existeDocumento(String uriDocumento) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescripcionDocumento(String uriDocumento) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTipoDocumento(String uriDocumento) throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = getVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
    private String getEndPoint() {
        return FapProperties.get("fap.platino.gestordocumental.url");
    }
	

    private String getVersion() {
        return gestorDocumentalPort.getVersion();
    }
    
    protected SGRDEServicePortType getGestorDocumentalPort(){
    	return this.gestorDocumentalPort;
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
    
//    private List<PropiedadesDocumento> obtenerPropiedadesDocumentos(String expediente) throws GestorDocumentalServiceException {
//        //String procedimiento = propertyPlaceholder.get("fap."+propertyPlaceholder.get("fap.defaultAED")+".procedimiento");
//    	String uriProcedimiento = FapProperties.get("fap.aed.procedimientos.procedimiento.uri");
//    	List<PropiedadesDocumento> lista = gestorDocumentalPort.buscarDocumentos(expediente, arg1);
//        if(lista == null)
//            lista = Collections.emptyList();
//        return lista;
//    }
    
    /**
     * Método que pasa a hex el primer número después de una / en las direcciones de búsqueda de Platino (expedientes)
     * @param uriExpediente
     * @return 
     */
    
    public String convertoToHex  (String uriExpediente){
    	String uriHex = "Ruta: \"";
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
    	return uriHex+"*\"";
    }
    
    
    private static boolean isNumeric(String cadena){
    	try {
    		Integer.parseInt(cadena);
    		return true;
    	} catch (NumberFormatException nfe){
    		return false;
    	}
    }
}
