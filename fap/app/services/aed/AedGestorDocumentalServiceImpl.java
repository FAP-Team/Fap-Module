package services.aed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPFaultException;

import models.InformacionRegistro;
import models.RepresentantePersonaJuridica;
import models.SolicitudGenerica;
import models.Tramite;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import platino.PlatinoProxy;
import play.libs.MimeTypes;
import properties.PropertyPlaceholder;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import utils.BinaryResponse;
import utils.StreamUtils;
import utils.WSUtils;
import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.aed.ws.dominio.DocumentoEnUbicacion;
import es.gobcan.eadmon.aed.ws.dominio.Expediente;
import es.gobcan.eadmon.aed.ws.dominio.Ubicaciones;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Contenido;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Documento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.RegistroDocumento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.TipoPropiedadAvanzadaEnum;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;

import static com.google.common.base.Preconditions.*;

/**
 * 
 */
public class AedGestorDocumentalServiceImpl implements GestorDocumentalService {

	private final AedPortType aedPort;

	private final PropertyPlaceholder propertyPlaceholder;

	private final ProcedimientosService procedimientosService;
	
	private static final Logger log = Logger.getLogger(AedGestorDocumentalServiceImpl.class);

    private final TiposDocumentosService tiposDocumentos;
	
    @Inject
	public AedGestorDocumentalServiceImpl(PropertyPlaceholder propertyPlaceholder){
		play.Logger.info("gestorDocumentalServiceImpl constructor");
		
	    this.propertyPlaceholder = propertyPlaceholder;
		
        URL wsdlURL = Aed.class.getClassLoader().getResource("aed/aed.wsdl");
        this.aedPort = new Aed(wsdlURL).getAed(new MTOMFeature());
        WSUtils.configureEndPoint(aedPort, getEndPoint());
        PlatinoProxy.setProxy(aedPort, propertyPlaceholder);
        
        tiposDocumentos = new TiposDocumentosService(propertyPlaceholder);
        procedimientosService = new ProcedimientosService(propertyPlaceholder, tiposDocumentos);
	}

    private String getEndPoint() {
        return propertyPlaceholder.get("fap.aed.url");
    }
	
    /**
     * Configura el servicio
     * 
     * Crea la carpeta para almacenar los documentos temporales
     */
    @Override
    public void configure() throws GestorDocumentalServiceException {
        try {
            if(!existeCarpetaTemporal()){
                crearCarpetaTemporal();
            }
        }catch(AedExcepcion e){
            throw serviceExceptionFrom(e);
        }
    }
    
    /**
     * Comprueba que el servicio esté configurado
     * 
     * El servicio está configurado si tiene conección con el servicio web
     * y está la carpeta temporal creada
     */
    @Override
	public boolean isConfigured(){
	    boolean isConfigured; 
	    try {
	        isConfigured =  hasConnection() && existeCarpetaTemporal();
	    }catch(AedExcepcion e){
	        isConfigured = false;
	    }
	    return isConfigured;
	}
		
	private boolean hasConnection(){
        boolean hasConnection = false;
        try {
            hasConnection = getVersion() != null;
        }catch(Exception e){
            log.info("El servicio no tiene coneccion con " + getEndPoint());
        }
		return hasConnection;
	}
	
	private String getVersion() throws AedExcepcion {
        Holder<String> version = new Holder<String>();
        Holder<String> revision = new Holder<String>();
        aedPort.obtenerVersionServicio(version, revision);
        return version.value + " - " + revision.value;
	}
	
	/**
	 * Crea el expediente para la solicitud
	 * 
	 * Al expediente se le asignaran como interesados el solicitante
	 * y todos sus representantes.
	 * 
	 * @param  solicitud
	 * @return número de expediente asignado
	 * 
	 * @throws NullPointerException El solicitante o alguno de sus representantes no tiene los campos 
	 *                              nombreCompleto o númeroId
	 * @throws GestorDocumentalServiceException Si el servicio web dió error                             
	 */
	@Override
    public String crearExpediente(SolicitudGenerica solicitud) throws GestorDocumentalServiceException {        
        Interesados interesados = getInteresados(solicitud);
        String numeroExpediente = solicitud.expedienteAed.asignarIdAed();
        String procedimiento = propertyPlaceholder.get("fap.aed.procedimiento");
        String convocatoria = propertyPlaceholder.get("fap.aed.convocatoria");

        Expediente expediente = new Expediente();
        expediente.setIdExterno(numeroExpediente);
        expediente.setProcedimiento(procedimiento);
        expediente.setValorModalidad(convocatoria);
        expediente.getInteresados().addAll(interesados.getDocumentos());
        expediente.getInteresadosNombre().addAll(interesados.getNombres());
        
        try {
            aedPort.crearExpediente(expediente);
            log.info("Creado expediente " + numeroExpediente + " para la solicitud " + solicitud.id);
        }catch(AedExcepcion e){
            throw new GestorDocumentalServiceException("Error creando expediente " + numeroExpediente + " para la solicitud " + solicitud.id, e);
        }
        return numeroExpediente;
    }

    private Interesados getInteresados(SolicitudGenerica solicitud){
        Interesados interesados = new Interesados();
        interesados.add(solicitud.solicitante);
        
        boolean representado = solicitud.solicitante.representado != null ? solicitud.solicitante.representado : false;
        //Representantes
        if(solicitud.solicitante.isPersonaFisica() && representado){
            interesados.add(solicitud.solicitante.representante);
        }else if(solicitud.solicitante.isPersonaJuridica()){
            for(RepresentantePersonaJuridica representante: solicitud.solicitante.representantes){
                interesados.add(representante);
            }
        }
        return interesados;
    }
    
    
    /**
     * Recupera las uris de los documentos que están en un expediente
     * 
     * @return Lista de uris
     * 
     * @throws GestorDocumentalServiceException si el servicios web da error
     */
    @Override
    public List<String> getDocumentosEnExpediente(String expediente) throws GestorDocumentalServiceException {
        List<String>  uris = new ArrayList<String>();
        try {
            List<PropiedadesDocumento> lista = obtenerPropiedadesDocumentos(expediente);
            for(PropiedadesDocumento propiedades : lista){
                uris.add(propiedades.getUri());
            }
        }catch(AedExcepcion e){
            throw new GestorDocumentalServiceException("Error al recuperar los documentos del expediente " + expediente, e);
        }
        return uris;
    }
    
    private List<PropiedadesDocumento> obtenerPropiedadesDocumentos(String expediente) throws AedExcepcion {
        String procedimiento = propertyPlaceholder.get("fap.aed.procedimiento");
        List<PropiedadesDocumento> lista = aedPort.buscarDocumentos(procedimiento, expediente, null, null, null, null, null, null, null);
        if(lista == null)
            lista = Collections.emptyList();
        return lista;
    }
	
    /**
     * Recupera el contenido de un documento
     * 
     * El documento puede estar clasificado o no clasificado
     * 
     * @param documento
     * @return Contenido y nombre del fichero
     * 
     * @throws GestorDocumentalServiceException Si se produjo un error al recuperar un documento
     * 
     */
    @Override
    public BinaryResponse getDocumento(models.Documento documento) throws GestorDocumentalServiceException {
        boolean clasificado = documento.clasificado;
        BinaryResponse response = new BinaryResponse();
        try {
            Documento doc;
            if (!clasificado)
                doc = aedPort.obtenerDocumentoNoClasificado(documento.uri);
            else
                doc = aedPort.obtenerDocumento(documento.uri);
            
            response.contenido = doc.getContenido().getFichero();
            response.nombre = doc.getContenido().getNombre();
        } catch (AedExcepcion e) {
            throw new GestorDocumentalServiceException("No se ha podido cargar el documento " + documento.uri + " clasificado= "
                    + clasificado + " - error: " + getLogMessage(e), e);
        }        
        log.info("Documento recuperado del aed " + documento.uri);
        return response;
    }
    
    private boolean isClasificado(models.Documento documento) throws GestorDocumentalServiceException {
        if(documento.clasificado == null){
            throw new GestorDocumentalServiceException("No se tiene información de si el documento con uri " + documento.uri + " está o no clasificado");
        }
        return documento.clasificado;        
    }

    /**
     * Almacena un documento temporal
     */
	@Override
	public String saveDocumentoTemporal(models.Documento documento, InputStream contenido, String filename) throws GestorDocumentalServiceException {
		//Preparamos el documento para subir al AED		
        documento.prepararParaSubir();

        checkNotNull(documento.tipo, "tipo del documento no puede ser null");
        checkNotNull(documento.descripcion, "descripcion del documento no puede ser null");
        checkNotNull(contenido, "contenido no puede ser null");
        checkNotNull(filename, "filename del documento no puede ser null");
        
        checkArgument(!documento.tipo.isEmpty(), "El tipo de documento no puede estar vacío");
        checkArgument(!documento.descripcion.isEmpty(), "La descripción del documento no puede estar vacía");
        checkArgument(!filename.isEmpty(), "El filename no puede estar vacío");
        
        checkDocumentoNotInGestorDocumental(documento);
        checkNotEmptyImputStream(contenido);
		
		Documento documentoAed = crearDocumentoTemporal(documento.tipo, documento.descripcion, filename, contenido);
		
		String ruta = propertyPlaceholder.get("fap.aed.temporales");
		String uri = null;
		try {
    		uri = aedPort.crearDocumentoNoClasificado(ruta, documentoAed);
    
    		documento.uri = uri;
    		documento.fechaSubida = new DateTime();
    		documento.clasificado = false;
    		
    		// Almacena el Hash del documento
    		documento.hash=getHash(uri);
    		
    		//Guarda el documento
    		documento.save();
    		log.debug("Documento temporal creado uri=" + uri);
        }catch(AedExcepcion e){
            throw serviceExceptionFrom(e);
        }
		
		return uri;		
	}
	
    private void checkDocumentoNotInGestorDocumental(models.Documento documento) throws GestorDocumentalServiceException {
        if(documento.uri != null){
            throw new GestorDocumentalServiceException("El documento ya tiene uri, ya está subido al gestor documental");
        }        
    }
    
    private void checkNotEmptyImputStream(InputStream is) throws GestorDocumentalServiceException {
        try {
            if(is.available() <= 0){
                throw new GestorDocumentalServiceException("El fichero está vacio");
            }
        } catch (IOException e) {
            throw new GestorDocumentalServiceException("Error al comprobar si el fichero está disponible");
        }
    }
	
	@Override
    public String saveDocumentoTemporal(models.Documento documento, File file) throws GestorDocumentalServiceException {
        try {
            return saveDocumentoTemporal(documento, new FileInputStream(file), file.getName());
        } catch (FileNotFoundException e) {
            throw new GestorDocumentalServiceException("File not found", e);
        }
    }
	
	private Documento crearDocumentoTemporal(String tipo, String descripcion, String filename, InputStream is){
        Documento documento = new Documento();
        
        // Propiedades básicas
        documento.setPropiedades(new PropiedadesDocumento());
        documento.getPropiedades().setDescripcion(descripcion);
        documento.getPropiedades().setUriTipoDocumento(tipo);

        // Propiedades avanzadas
        documento.getPropiedades().setTipoPropiedadesAvanzadas(TipoPropiedadAvanzadaEnum.ADMINISTRATIVO);
        PropiedadesAdministrativas propiedadesAdministrativas = new PropiedadesAdministrativas();
        documento.getPropiedades().setPropiedadesAvanzadas(propiedadesAdministrativas);
        propiedadesAdministrativas.getInteresados().add(propertyPlaceholder.get("fap.aed.documentonoclasificado.interesado.nombre"));
        propiedadesAdministrativas.getInteresadosNombre().add(propertyPlaceholder.get("fap.aed.documentonoclasificado.interesado.nif"));
        
        // Contenido
        Contenido contenido = new Contenido();
        contenido.setNombre(filename);
        String mime = MimeTypes.getMimeType(filename, "application/octet-stream");
        contenido.setFichero(StreamUtils.getDataHandler(is, mime));
        contenido.setTipoMime(mime);        
        documento.setContenido(contenido);
        return documento;
	}

	private String getHash(String uri) throws AedExcepcion {
        PropiedadesDocumento pro  = aedPort.obtenerDocumentoPropiedadesNoClasificado(uri);
        String hash = ((PropiedadesAdministrativas)pro.getPropiedadesAvanzadas()).getSellado().getHash();
        return hash;   
	}
	
    @Override
    public void updateDocumento(models.Documento documento) throws GestorDocumentalServiceException {
        documento.prepararParaSubir();

        try {
            boolean clasificado = isClasificado(documento);
            PropiedadesDocumento props = obtenerPropiedades(documento.uri, clasificado);
            
            props.setDescripcion(documento.descripcion);
            props.setUriTipoDocumento(documento.tipo);
            if(clasificado){
                List<DocumentoEnUbicacion> ubicaciones = aedPort.obtenerDocumentoRutas(documento.uri);
                List<Ubicaciones> newUbicaciones = clonarUbicaciones(ubicaciones);
                aedPort.actualizarDocumentoPropiedades(props, newUbicaciones);
            }else{
                aedPort.actualizarDocumentoPropiedadesNoClasificado(props);
            }    
        }catch(AedExcepcion e){
            throw serviceExceptionFrom(e);
        }
    }

	private List<Ubicaciones> clonarUbicaciones(List<DocumentoEnUbicacion> documentoUbicaciones){
        List<Ubicaciones> result = new ArrayList<Ubicaciones>();
        for (DocumentoEnUbicacion docUbic : documentoUbicaciones) {
            Ubicaciones ubic = new Ubicaciones();
            ubic.setProcedimiento(docUbic.getProcedimiento());
            ubic.getExpedientes().add(docUbic.getExpediente());
            result.add(ubic);
        }
        return result;
	}
	
	private PropiedadesDocumento obtenerPropiedades(String uri, boolean clasificado) throws AedExcepcion {
		PropiedadesDocumento propiedades;
		if(clasificado){
			propiedades = aedPort.obtenerDocumentoPropiedades(uri);
		}else{
			propiedades = aedPort.obtenerDocumentoPropiedadesNoClasificado(uri);
		}
		return propiedades;
		
	}
	
    @Override
    public void deleteDocumento(models.Documento documento) throws GestorDocumentalServiceException {
        if(documento == null || documento.uri == null || documento.clasificado == null){
            throw new NullPointerException();
        }
        
        log.debug("Borrando documento con uri " + documento.uri);
        if(documento.clasificado){
            throw new GestorDocumentalServiceException("Los documentos clasificados no se pueden eliminar");
        }
        
        try {
            aedPort.suprimirDocumentoNoClasificado(documento.uri);
        }catch(AedExcepcion e){
            throw serviceExceptionFrom(e);
        }
    }	
	
    private static String getLogMessage(AedExcepcion e){
        return e.getFaultInfo().getDescripcion();
    }
	
    private static GestorDocumentalServiceException serviceExceptionFrom(AedExcepcion e){
        return new GestorDocumentalServiceException(getLogMessage(e), e);
    }
	
    @Override
    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, InformacionRegistro informacionRegistro) throws GestorDocumentalServiceException {
        log.debug("Clasificando documentos");
        String idAed = solicitud.expedienteAed.idAed;
        
        if(idAed == null)
            throw new NullPointerException();
        
        Interesados interesados = getInteresados(solicitud);
        
        boolean todosClasificados = true;
        String errores = "";
        for(models.Documento documento : documentos){
            if(!documento.clasificado){
                try {
                    if(informacionRegistro == null){
                        clasificarDocumentoSinRegistro(idAed, documento, interesados);
                    }else{
                        //TODO: Pasar parámetro notificable
                        clasificarDocumentoConRegistro(idAed, documento, interesados, informacionRegistro, false); 
                    }
                }catch(AedExcepcion e){
                    todosClasificados = false;
                    errores += "Error al clasificar el documento " + documento.uri + "\n";
                }catch(SOAPFaultException e){
                    todosClasificados = false;
                    errores += "Error al clasificar el documento " + documento.uri + "\n";
                    e.printStackTrace();
                }
            }else{
                log.warn("El documento " + documento.uri + " ya está clasificado");
            }
        }
        if(!todosClasificados){
            throw new GestorDocumentalServiceException("No se pudieron clasificar todos los documentos : " + errores);
        }
    }
    
    @Override
    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos) throws GestorDocumentalServiceException {
        clasificarDocumentos(solicitud, documentos, null);
    }
    
    private void clasificarDocumentoSinRegistro(String idAed, models.Documento documento, Interesados interesados) throws AedExcepcion {
        PropiedadesDocumento propiedades = obtenerPropiedades(documento.uri, documento.clasificado);
        clasificarDocumento(idAed, documento, propiedades, interesados);
    }

    private void clasificarDocumentoConRegistro(String idAed, models.Documento documento, Interesados interesados, InformacionRegistro informacionRegistro, boolean notificable) throws AedExcepcion {
        PropiedadesDocumento propiedades = obtenerPropiedades(documento.uri, documento.clasificado);
        PropiedadesAdministrativas propAdmin = (PropiedadesAdministrativas) propiedades.getPropiedadesAvanzadas();

        // Asigna la información de registro
        RegistroDocumento registro = new RegistroDocumento();
        registro.setFechaRegistro(informacionRegistro.fechaRegistro.toDate());
        registro.setNumRegistro(informacionRegistro.numeroRegistro);
        registro.setNumRegistroGeneral(informacionRegistro.numeroRegistroGeneral);
        registro.setUnidadOrganica(informacionRegistro.unidadOrganica);
        propAdmin.setRegistro(registro);

        // Marca como notificable
        if (notificable)
            propAdmin.setNotificable(true);

        clasificarDocumento(idAed, documento, propiedades, interesados);
    }
    
    private void clasificarDocumento(String idAed, models.Documento documento, PropiedadesDocumento propiedadesDocumento, Interesados interesados) throws AedExcepcion {
        // Registro de entrada
        PropiedadesAdministrativas propsAdmin = (PropiedadesAdministrativas)propiedadesDocumento.getPropiedadesAvanzadas();

        // Documentos pasan a ser del interesado no del user
        propsAdmin.getInteresados().clear();
        propsAdmin.getInteresados().addAll(interesados.getDocumentos());
        propsAdmin.getInteresadosNombre().clear();
        propsAdmin.getInteresadosNombre().addAll(interesados.getNombres());

        // Ubicaciones
        String procedimiento = propertyPlaceholder.get("fap.aed.procedimiento");
        List<Ubicaciones> ubicaciones = new ArrayList<Ubicaciones>();
        Ubicaciones ubicacionExpediente = new Ubicaciones();
        ubicacionExpediente.setProcedimiento(procedimiento);
        ubicacionExpediente.getExpedientes().add(idAed);
        ubicaciones.add(ubicacionExpediente);

        // Clasificar documento al expediente
        aedPort.clasificarDocumento(documento.uri, propiedadesDocumento, ubicaciones);
        documento.clasificado = true;
        documento.save();
        
        log.info("Documento temporal clasificado: Expediente: " + idAed + ", Documento: " + documento.uri);
    }
	
    @Override
    public void agregarFirma(models.Documento documento, models.Firma firma) throws GestorDocumentalServiceException {
        if(firma.getContenido() == null)
            throw new GestorDocumentalServiceException("La firma está vacia");
        
        if(!firma.isFirmaSimple())
            throw new GestorDocumentalServiceException("La firma debe tener un único firmante");
        
        try {
            PropiedadesDocumento propiedadesDocumento = obtenerPropiedades(documento.uri, documento.clasificado);
            PropiedadesAdministrativas propiedadesAdministrativas = (PropiedadesAdministrativas)propiedadesDocumento.getPropiedadesAvanzadas();
            Firma firmaActual = propiedadesAdministrativas.getFirma();
            
            models.Firmante firmante = firma.getFirmantes().get(0);
            
            Firma firmaNueva = concatenarFirma(firmaActual, firmante, firma.getContenido());
            propiedadesAdministrativas.setFirma(firmaNueva);
    
            boolean clasificado = isClasificado(documento);
            actualizarPropiedades(propiedadesDocumento, clasificado);
        }catch(AedExcepcion e){
            throw serviceExceptionFrom(e);
        }
    }
    
    private Firma concatenarFirma(Firma firma, models.Firmante firmante, String nueva){
        Firma firmaNueva = null;
        if(firma == null)
            firmaNueva = firmaSimple(nueva);
        else
            firmaNueva = firmaParalela(firma, nueva);
        asignarFirmante(firmaNueva, firmante);
        return firmaNueva;
    }
    
    private Firma firmaSimple(String nueva){
        Firma firma = new Firma();
        firma.setContenido(nueva);
        firma.setTipoMime("text/xml");
        return firma;
    }
    
    private Firma firmaParalela(Firma actual, String nueva){
        Firma firma = actual;
        // No es la primera construimos firma paralela
        String firmaParalela = "<SignatureList>";
        String firmaOld = new String(firma.getContenido().getBytes());
        firmaOld = firmaOld.replaceFirst("<\\?.*\\?>", "");
        firmaOld = firmaOld.replaceFirst("<SignatureList>", "");
        firmaOld = firmaOld.replaceFirst("</SignatureList>", "");
        firmaParalela += firmaOld;
        firmaParalela += nueva.replaceFirst("<\\?.*\\?>", "");
        firmaParalela += "</SignatureList>";
        firma.setContenido(firmaParalela); 
        firma.setTipoMime("text/xml");
        return firma;
    }
	
    private void asignarFirmante(Firma firma, models.Firmante firmante){
        es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante firmanteAed = new es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante();
        firmanteAed.setFirmanteNombre(firmante.nombre);
        firmanteAed.setFirmanteNif(firmante.idvalor);
        firmanteAed.setFecha(firmante.fechaFirma.toDate());
        firma.getFirmantes().add(firmanteAed); // puede haber firmas anteriores        
    }
    
	private void actualizarPropiedades(PropiedadesDocumento propiedades, boolean clasificado) throws AedExcepcion {
        if(clasificado){
            //TODO falta ver las ubicaciones y si se incrementa la versión del documento
            //aedPort.actualizarDocumentoPropiedades(propiedades, arg1)
        }else{
            aedPort.actualizarDocumentoPropiedadesNoClasificado(propiedades);
        }
	}
	
	/**
	 * Recupera la firma de un documentos
	 * @throws GestorDocumentalServiceException si no se pueden recuperar las propiedades del documento
	 */
    @Override
    public models.Firma getFirma(models.Documento documento) throws GestorDocumentalServiceException {
        boolean clasificado = isClasificado(documento);
        
        PropiedadesDocumento propiedades;
        try {
            propiedades = obtenerPropiedades(documento.uri, clasificado);
        } catch (AedExcepcion e) {
            throw serviceExceptionFrom(e);
        }
        PropiedadesAdministrativas propsAdmin = ((PropiedadesAdministrativas) propiedades.getPropiedadesAvanzadas());
        Firma firmaAed = propsAdmin.getFirma();
        models.Firma result = null;
        if(firmaAed != null){
            result = toFirmaModel(firmaAed);
        }
        return result;
    }

    private models.Firma toFirmaModel(Firma firmaAed){
        models.Firma result = new models.Firma(firmaAed.getContenido(), toModelFirmantes(firmaAed.getFirmantes()));
        return result;
    }
    
    private List<models.Firmante> toModelFirmantes(List<Firmante> firmantesAed){
        List<models.Firmante> firmantes = new ArrayList<models.Firmante>();
        for(Firmante firmanteAed : firmantesAed){
            models.Firmante firmante = new models.Firmante();
            firmante.nombre = firmanteAed.getFirmanteNombre();
            firmante.fechaFirma = new DateTime(firmanteAed.getFecha());
            firmante.idvalor = firmanteAed.getFirmanteNif();
            firmantes.add(firmante);
        }
        return firmantes;
    }

    /**
     * Recupera la información de los trámites
     * @return Tramites
     * @throws GestorDocumentalServiceException si no se pudo recuperar la información
     */
    @Override
    public List<Tramite> getTramites() throws GestorDocumentalServiceException {
        List<Tramite> tramites = procedimientosService.getTramites();
        return tramites;
    }
    
    /**
     * Crea la carpeta temporal en el aed que está configurada
     * en la property "fap.aed.temporales"
     * 
     * @throws AedExcepcion
     */
    private void crearCarpetaTemporal() throws AedExcepcion {
        String carpeta = propertyPlaceholder.get("fap.aed.temporales");
        if (carpeta == null || carpeta.isEmpty()) {
            throw new IllegalStateException(
                    "La property fap.aed.temporales no está configurada en el application.conf");
        }
        crearCarpetaTemporal(carpeta);
    }
	

    /**
     * Crea una carpeta donde se van a almacenar los documentos temporales
     * que su suban en el archivo.
     * 
     * El método del servicio no crea carpeta en varios niveles de profundidad
     * hay que ir creando la carpeta de cada nivel.
     * 
     * @param carpeta
     * @throws AedExcepcion
     */
    private void crearCarpetaTemporal(String carpeta) throws AedExcepcion {
        if(carpeta == null)
            throw new NullPointerException();
        if(carpeta.isEmpty())
            throw new IllegalArgumentException();
        
        String[] splits = carpeta.split("/");
        String ruta = "";
        for(String s : splits){
            aedPort.crearCarpetaNoClasificada(ruta, s, null);
            ruta = ruta.isEmpty() ? s  : ruta + "/" + s;
        }
    }
    
    /**
     * Comprueba si existe la carpeta temporal definida en la property
     * "fap.aed.temporales"
     */
    private boolean existeCarpetaTemporal() throws AedExcepcion {
        String carpeta = propertyPlaceholder.get("fap.aed.temporales");
        return existeCarpetaTemporal(carpeta);
    }

    /**
     * Comprueba si existe uan carpeta no clasificada en el archivo
     * @param carpeta Ruta de la carpeta que se va a comprobar
     */
    private boolean existeCarpetaTemporal(String carpeta) throws AedExcepcion {
        if(carpeta == null)
            throw new NullPointerException();
        if(carpeta.isEmpty())
            throw new IllegalArgumentException();
        
        boolean result = false;
        try {
            aedPort.obtenerCarpetasNoClasificadas(carpeta);
            //Si no da una excepción, la carpeta existe
            result = true;
        }catch(AedExcepcion e){
            if(e.getFaultInfo().getCodigoError() != CodigoErrorEnum.CARPETA_NO_EXISTE){
                throw e;
            }
        }
        return result;
    }
    
    /**
     * Borra una carpeta temporal
     * @param carpeta
     * @throws AedExcepcion
     */
    private void borrarCarpetaTemporal(String carpeta) throws AedExcepcion {
        if(carpeta == null)
            throw new NullPointerException();
        if(carpeta.isEmpty())
            throw new IllegalArgumentException();
        
        String path, folder;
        if(carpeta.contains("/")){
            int index = carpeta.lastIndexOf('/');
            path = carpeta.substring(0, index - 1);
            folder = carpeta.substring(index + 1, carpeta.length());
        }else{
            path = "";
            folder = carpeta;
        }
        try {
            aedPort.suprimirCarpetaNoClasificada(path, folder);
        } catch(AedExcepcion e){
            if(e.getFaultInfo().getCodigoError() != CodigoErrorEnum.CARPETA_NO_EXISTE)
                throw e;
        }
    }
	
}
