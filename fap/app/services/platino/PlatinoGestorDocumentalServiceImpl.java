package services.platino;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;

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
import es.gobcan.platino.servicios.sgrde.DocumentoSimple;
import es.gobcan.platino.servicios.sgrde.ElementoNoEncontradoException;
import es.gobcan.platino.servicios.sgrde.ErrorInternoException;
import es.gobcan.platino.servicios.sgrde.Expediente;
import es.gobcan.platino.servicios.sgrde.FirmasElectronicas;
import es.gobcan.platino.servicios.sgrde.InformacionFirmaElectronica;
import es.gobcan.platino.servicios.sgrde.Interesado;
import es.gobcan.platino.servicios.sgrde.MetaInformacionException;
import es.gobcan.platino.servicios.sgrde.RutaNoValidaException;
import es.gobcan.platino.servicios.sgrde.SGRDEServicePortType;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;
import es.gobcan.platino.servicios.sgrde.TamanoMaximoExcedidoException;
import es.gobcan.platino.servicios.sgrde.TipoContenidoNoPermitidoException;
import es.gobcan.platino.servicios.sgrde.UsuarioNoValidoException;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.ExpedienteAed;
import models.ExpedientePlatino;
import models.Firma;
import models.InformacionRegistro;
import models.Persona;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import models.Tramite;
import services.FirmaService;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.filesystem.TipoDocumentoEnTramite;
import utils.BinaryResponse;
import utils.WSUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import config.InjectorConfig;
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
        private PlatinoProcedimientosServiceImpl procedimientosPort;	   
        private PlatinoSoporteTramitacionServiceImpl tramitacionPort;

	    @Inject
	    public PlatinoGestorDocumentalServiceImpl(PropertyPlaceholder propertyPlaceholder) {
	        this.propertyPlaceholder = propertyPlaceholder;

	        URL wsdlURL = PlatinoGestorDocumentalServiceImpl.class.getClassLoader().getResource("wsdl/sgrde.wsdl");
	        gestorDocumentalPort = new SGRDEServiceProxy(wsdlURL).getSGRDEServiceProxyPort(new MTOMFeature());
	        
	        WSUtils.configureEndPoint(gestorDocumentalPort, getEndPoint());
	        WSUtils.configureSecurityHeaders(gestorDocumentalPort, propertyPlaceholder);
	        PlatinoProxy.setProxy(gestorDocumentalPort, propertyPlaceholder);
	        
	        Client client = ClientProxy.getClient(gestorDocumentalPort);
			HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
			httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
			httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
			httpConduit.setClient(httpClientPolicy);	
			
			procedimientosPort = InjectorConfig.getInjector().getInstance(PlatinoProcedimientosServiceImpl.class);
			procedimientosPort.mostrarInfoInyeccion();
			
			tramitacionPort = InjectorConfig.getInjector().getInstance(PlatinoSoporteTramitacionServiceImpl.class);
			tramitacionPort.mostrarInfoInyeccion();
			
	    }


	// Configura el gestorDocumental
	@Override
	public void configure() throws GestorDocumentalServiceException {
		if (!procedimientosPort.buscarProcedimientos(FapProperties.get("fap.platino.procedimientos.procedimiento")));
			throw new GestorDocumentalServiceException("El procedimiento "+FapProperties.get("fap.aed.procedimientos.procedimiento.uri")+" no existe en la BDProcedimientos de Platino");
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
		
		/**
		 * TODO
		 * AQUI, debería tener en cuenta que el GDPlatino sea el gestorDoc inyectado, para no crear el
		 * expediente dos veces para la misma solicitud
		 * 
		 */
		
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

	/*
	 * METODO INCOMPLETO FALTA EL INTERESADO <-------------
	 * @see services.GestorDocumentalService#getDocumentosPorTipo(java.lang.String)
	 */
	
	@Override
	public List<Documento> getDocumentosPorTipo(String tipoDocumento) throws GestorDocumentalServiceException {
		if (tipoDocumento == null || tipoDocumento.isEmpty())
			return Collections.emptyList();
			
		Agente agente = AgenteController.getAgente();
			
		// 1) Añadiendo el procedimiento a la consulta
		String consulta = FapProperties.get("fap.platino.gestordocumental.procedimiento");
		// Conjunto de documentos donde su tipo es 'tipoDocumento' y el interesado es el usuario logueado
		List<String> listaDocsUris;
		List<models.Documento> listaDocumentos = new ArrayList<models.Documento>();
		try {
			//Buscar por procedimiento, tipoDoc e interesado 
			// 2) Añadiendo el interesado a la consulta. Necesito doc con Interesado para probar
			// 2.1) Obtener la persona a la que coressponde ese agente y crear interesado para la búsqueda. 
			
			play.Logger.info("Buscando documentos en Platino para:"+
					" Agente "+agente.username+
					" Procedimiento: "+FapProperties.get("fap.platino.gestordocumental.procedimiento")+
					" Tipo de documento: "+tipoDocumento);
			
			Interesado interesado = new Interesado();
			interesado.setIdInteresado(agente.username); //dni, cif,..
			interesado.setDescInteresado(agente.name);
			
			// TODO consulta +="Interesado: \""; //Tira de agente
			// 3) Añadiendo el tipo de documento a la consulta
			consulta+= ", Tipo_Doc: \""+tipoDocumento+"\"";
			// Obtengo lista de uris que cumplen las tres condiciones
			listaDocsUris = gestorDocumentalPort.buscarDocumentos(consulta, 90);
			
			if(listaDocsUris.isEmpty())
		   		return Collections.emptyList();
					
			for (String uri : listaDocsUris) {
				DocumentoBase metadatos = gestorDocumentalPort.obtenerMetaDoc(uri);
				System.out.println("DescripcionDoc: "+metadatos.getDescDoc());
				models.Documento doc = new models.Documento();
				doc.docPlatino2Doc(metadatos, tipoDocumento);
				listaDocumentos.add(doc);
				doc.delete();
			}
		} catch (ErrorInternoException e) {
			throw new GestorDocumentalServiceException("Error extrayendo los documentos por tipo");
			//e.printStackTrace();
		} catch (ElementoNoEncontradoException e) {
			play.Logger.error("Error obteniendo los metadatos de algún documento de respuesta");
			e.printStackTrace();
		} catch (UsuarioNoValidoException e) {
			play.Logger.error("Error, interesado no válido");
			e.printStackTrace();
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
	public String saveDocumentoTemporal(Documento documento, InputStream contenido, String filename, SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
		checkNotNull(documento.tipo, "tipo del documento no puede ser null");
	    checkNotNull(documento.descripcionVisible, "descripcion del documento no puede ser null");
	    checkNotNull(contenido, "contenido no puede ser null");
	    checkNotNull(filename, "filename del documento no puede ser null");
	        
	    checkArgument(!documento.tipo.isEmpty(), "El tipo de documento no puede estar vacío");
	    checkArgument(!documento.descripcionVisible.isEmpty(), "La descripción del documento no puede estar vacía");
	    checkArgument(!filename.isEmpty(), "El filename no puede estar vacío");
	    checkDocumentoNotInGestorDocumental(documento);
	    
//		La ruta sería el expediente donde se almacena -> Obtenerlo de Solicitud
		String ruta = solicitud.expedientePlatino.ruta+"/"+filename;
	
		DataHandler documentoDH = utils.StreamUtils.getDataHandler(contenido, "application/octet-stream");
		DocumentoSimple metainformacion = new DocumentoSimple();
		
		//TODO el tipo de solicitud tiene q venir de documento.tipo
		metainformacion.setTipoDoc("SOL");
		metainformacion.setDescDoc(documento.descripcion);
		metainformacion.setAdmiteVersionado(false);
		metainformacion.setTipoMime(documento.tipo);
		XMLGregorianCalendar fecha = null;
		try {
			fecha = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
			metainformacion.setFechaDoc(fecha);
			metainformacion.setDocCiudadano(true);
			metainformacion.setDocArchivo(false);
		} catch (DatatypeConfigurationException e) {
			play.Logger.error("Error intentando asignar la Fecha en la subida del documento al gestor documental de Platino");
			Messages.error("Error subiendo documento al gestor documental de Platino");
			e.printStackTrace();
		}	
		
		try {
			return gestorDocumentalPort.insertarDocumento(documentoDH, ruta, metainformacion);
		} catch (MetaInformacionException e) {
			play.Logger.error("Error: La metainformación del documento "+documento.uri);
			e.printStackTrace();
		} catch (UsuarioNoValidoException e) {
			play.Logger.error("Error: el usuario no es válido");
			e.printStackTrace();
		} catch (RutaNoValidaException e) {
			play.Logger.error("Error: La ruta no es válida "+ruta);
			e.printStackTrace();
		} catch (ErrorInternoException e) {
			play.Logger.error("Error: Error interno de Platino");
			e.printStackTrace();
		} catch (TamanoMaximoExcedidoException e) {
			play.Logger.error("Error: El documento excede el tamaño máximo permitido");
			e.printStackTrace();
		} catch (TipoContenidoNoPermitidoException e) {
			play.Logger.error("Error: El tipo del contenido del documento no está permitido");
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String saveDocumentoTemporal(Documento documento, File file, SolicitudGenerica solicitud)
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
    
    private void checkDocumentoNotInGestorDocumental(models.Documento documento) throws GestorDocumentalServiceException {
        if(documento.uri != null){
            throw new GestorDocumentalServiceException("El documento ya tiene uri, ya está subido al gestor documental de Platino");
        }        
    }
    
}
