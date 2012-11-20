package services.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

import controllers.fap.AgenteController;

import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.eadmon.procedimientos.ws.dominio.AportadoPorEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.CardinalidadEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.ObligatoriedadEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;

import play.Play;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.IO;
import play.vfs.VirtualFile;
import properties.FapProperties;
import properties.PropertyPlaceholder;

import models.Agente;
import models.Consulta;
import models.Documento;
import models.Documentacion;
import models.ExpedienteAed;
import models.Firma;
import models.Firmante;
import models.InformacionRegistro;
import models.Participacion;
import models.SolicitudGenerica;
import models.TipoCodigoExclusion;
import models.TipoDocumento;
import models.TiposCodigoRequerimiento;
import models.Tramite;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.aed.Interesados;
import utils.BinaryResponse;

import static com.google.common.base.Preconditions.*;

/**
 * Gestor documental en sistema de ficheros
 */
public class FileSystemGestorDocumentalServiceImpl implements GestorDocumentalService {
    
    private static final int URI_KEY_SIZE = 4;

    private final File temporalPath;
    
    private final File clasificadoPath;

    private final PropertyPlaceholder propertyPlaceholder;
    
    /**
     * @param path Carpeta donde irán las carpetas para los ficheros temporales y clasificados 
     */
    @Inject
    public FileSystemGestorDocumentalServiceImpl(PropertyPlaceholder propertyPlaceholder){
        String propertyPath = checkNotNull(propertyPlaceholder.get("fap.fs.gestorDocumental.path"));
        File path = Play.getFile(propertyPath);
        this.temporalPath = new File(path, "temporal");
        this.clasificadoPath = new File(path, "clasificado");
        
        play.Logger.info("Configurado FileSystem Gestor Documental");
        play.Logger.info("ruta documentos temporales %s", this.temporalPath.getAbsolutePath());
        play.Logger.info("ruta documentos clasificados %s", this.clasificadoPath.getAbsolutePath());
        
        this.propertyPlaceholder = propertyPlaceholder;
    }
        
    /**
     * Crea las carpetas de ficheros temporales y clasificados
     */
    @Override
    public void configure() throws GestorDocumentalServiceException {
        try {
            FileUtils.forceMkdir(temporalPath);
            FileUtils.forceMkdir(clasificadoPath);
        }catch(IOException e){
            throw new GestorDocumentalServiceException(e.getMessage(), e);            
        }            
    }
    
    /**
     * Comprueba si las carpetas temporal y clasificado existen
     */
    @Override
    public boolean isConfigured() {
        return temporalPath.exists() && clasificadoPath.exists();
    }
    
    @Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Gestor Documental ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Gestor Documental ha sido inyectado con FileSystem y NO está operativo.");
    }
    

    /**
     * Crea un expediente para la solicitud
     * @param Solicitud
     * @return numero de expediente asignado 
     */
    @Override
    public String crearExpediente(SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
        if(solicitud.solicitante == null)
            throw new NullPointerException();
        
        if(solicitud.solicitante.isPersonaFisica() && solicitud.solicitante.representado != null && solicitud.solicitante.representado && solicitud.solicitante.representante == null){
            throw new NullPointerException();
        }
        
        if(solicitud.solicitante.isPersonaJuridica() && solicitud.solicitante.representantes == null){
            throw new NullPointerException();
        }
        
        String expediente = solicitud.expedienteAed.asignarIdAed();
        File folder = getExpedienteFolder(expediente);
        try {
            FileUtils.forceMkdir(folder);
        }catch(IOException e){
            throw new GestorDocumentalServiceException("Error al crear la carpeta " + folder.getAbsolutePath());
        }
        return expediente;
    }

    private File getExpedienteFolder(String expediente){
        File folder = new File(clasificadoPath, expediente);
        return folder;
    }
    
    /**
     * Obtiene las uris de los documentos que hay en un expediente
     * @return lista de uris
     */
    @Override
    public List<String> getDocumentosEnExpediente(String expediente) throws GestorDocumentalServiceException {
        File folder = getExpedienteFolder(expediente);
        List<String> names = Arrays.asList(folder.list());
        return names;
    }
    
    /**
     * Obtiene las uris de los documentos de un determinado tipo y usuario (y que estén clasificados)
     * @return lista de uris
     */
    @Override
    public List<Documento> getDocumentosPorTipo(String tipoDocumento) {
    	List<Documento> rows = new ArrayList<Documento>();
    	if (tipoDocumento != null && !tipoDocumento.isEmpty()) {
	    	Agente agente = AgenteController.getAgente();
	        	
	    	// Documentos (de tipo tipoDocumento) de las solicitudes donde el agente actualmente logueado es solicitante 
	     	rows = Documento.find("select documento2 " +
	     			"from Documentacion documentacion2 join documentacion2.documentos documento2 " +
    				"where (documento2.tipo = '" + tipoDocumento + "') and  (documento2.clasificado = 1) and (documentacion2.id in " +
	     				// Documentación perteneciente a las solicitudes en los que el usuario actualmente logueado es solicitante 
	     				"(select documentacion.id from Documentacion documentacion, Solicitud solicitud " +
	     				"where (solicitud.documentacion.id = documentacion.id) and " +
	     				"(solicitud.id in " +
	     						// Seleccionamos los id de las solicitudes en los que el agente actualmente logueado es solicitante
		     					"(select solicitud2.id from Solicitud solicitud2 " +
		    					" where (solicitud2.solicitante.fisica.nip.valor = '" + agente.username + "') or " +
		    							"(solicitud2.solicitante.juridica.cif = '" + agente.username + "')" +
		    					")" +
	     				")" +
    				"))").fetch();
    	}
    	return rows;
    }
    
    /**
     * Obtiene el contenido de un documento
     * El documento puede estar clasificado o no clasificado
     * 
     * @return Contenido y nombre del documento
     */
    @Override
    public BinaryResponse getDocumento(Documento documento) throws GestorDocumentalServiceException {
        File file = getFile(documento);
        BinaryResponse response = BinaryResponse.fromFile(file);
        
        //Elimina el uuid
        String fileName = file.getName();
        response.nombre = fileName.substring(4, fileName.length());
        
        return response;
    }

    private File getDocumentoFolder(Documento documento){
        if(documento.clasificado.booleanValue()){
            return clasificadoPath;
        }else{
            return temporalPath;
        }
    }
    
    /**
     * Obtiene el contenido de un documento a partir de su uri
     * El documento puede estar clasificado o no clasificado
     * 
     * @return Contenido y nombre del documento
     */
    @Override
    public BinaryResponse getDocumentoByUri(String uriDocumento) throws GestorDocumentalServiceException {
    	Documento documento = Documento.findByUri(uriDocumento);
    	if (documento == null) {
    		throw new GestorDocumentalServiceException("No existe el documento con uri " + uriDocumento);
    	}
        File file = getFile(documento);
        BinaryResponse response = BinaryResponse.fromFile(file);
        
        //Elimina el uuid
        String fileName = file.getName();
        response.nombre = fileName.substring(4, fileName.length());
        
        return response;
    }
    
    
    private File getFile(Documento documento) throws GestorDocumentalServiceException{
        File folder = getDocumentoFolder(documento);
        File file = new File(folder, documento.uri);
        
        if(!file.exists())
            throw new GestorDocumentalServiceException("No existe el documento " + file.getAbsolutePath());
        
        return file;
    }
    
    /**
     * Almacena un documento en la carpeta temporal.
     * 
     * Asigna la uri y pone el documento como no clasificado.
     * 
     * @param documento 
     * @param contenido 
     * @param filename
     * 
     * @throws GestorDocumentalServiceException si el documento ya tiene uri
     */
    @Override
    public String saveDocumentoTemporal(Documento documento, InputStream contenido, String filename)
            throws GestorDocumentalServiceException {
    	
        checkNotNull(documento.tipo, "tipo del documento no puede ser null");
        checkNotNull(documento.descripcionVisible, "descripcion del documento no puede ser null");
        checkNotNull(contenido, "contenido no puede ser null");
        checkNotNull(filename, "filename del documento no puede ser null");        
        checkArgument(!documento.tipo.isEmpty(), "El tipo de documento no puede estar vacío");
        checkArgument(!documento.descripcionVisible.isEmpty(), "La descripción del documento no puede estar vacía");
        checkArgument(!filename.isEmpty(), "El filename no puede estar vacío");
        
        // No chequeamos si está en el gestor documental porque si refAed está a true significa que estamos
        // utilizando un archivo ya subido en el gestor documental para otra solicitud.
        if ( (documento.refAed == null) || (!documento.refAed.booleanValue()) ) 
        	checkDocumentoNotInGestorDocumental(documento);
        checkNotEmptyImputStream(contenido);

        String uri = Codec.UUID().substring(0, URI_KEY_SIZE) + filename;
        
        File file = new File(temporalPath, uri);
            
        try{
        	IO.write(contenido, file);
        }catch(Exception e){e.printStackTrace();};

        documento.uri = uri;
        documento.clasificado = false;
        documento.hash = Codec.UUID();
        documento.fechaSubida = new DateTime();
		documento.refAed = false;
        documento.save();

        return uri;
    }

    private void checkDocumentoNotInGestorDocumental(Documento documento) throws GestorDocumentalServiceException {
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
    public String saveDocumentoTemporal(Documento documento, File file) throws GestorDocumentalServiceException {
        try {
            return saveDocumentoTemporal(documento, new FileInputStream(file), file.getName());
        } catch (FileNotFoundException e) {
            throw new GestorDocumentalServiceException("File not found", e);
        }
    }
    
    /**
     * Actualiza la información del documento
     * 
     * Esta implementación no almacena está información.
     */
    @Override
    public void updateDocumento(Documento documento) throws GestorDocumentalServiceException {
        
    }

    /**
     * Elimina un documento
     * 
     * Únicamente se pueden eliminar documentos no clasificados
     * 
     * @param documento
     * 
     * @throws GestorDocumentalServiceException Si el documento está clasificado o el documento no se pudo clasificar
     */
    @Override
    public void deleteDocumento(Documento documento) throws GestorDocumentalServiceException {
        if(documento.uri == null)
            return; //El documento no está en el gestor documental
        
        if(documento.clasificado)
            throw new GestorDocumentalServiceException("No se puede eliminar un documento clasificado");
        
        File folder = getDocumentoFolder(documento);
        File file = new File(folder, documento.uri);
        if(file.exists()){
            boolean deleted = file.delete();
            if(!deleted)
                throw new GestorDocumentalServiceException("Error borrando el documento " + file.getAbsolutePath());
        }
    }

    /**
     * Clasifica una lista de documentos temporales
     * 
     * @param solicitud Solicitud para obtener el número de expediente
     * @param documentos Lista de documentos que se van a clasificar
     * @param informacionRegistro La información de registro no se utiliza en esta implementación
     * 
     * @return true si se pudieron clasificar todos los documentos
     */
    @Override
    public void clasificarDocumentos(SolicitudGenerica solicitud, List<Documento> documentos,
            InformacionRegistro informacionRegistro) throws GestorDocumentalServiceException {
        clasificarDocumentos(solicitud, documentos);
    }

    private boolean move(File src, File dstFolder){
        return src.renameTo(new File(dstFolder, src.getName()));
    }
    
    /**
     * Clasifica una lista de documentos temporales
     * 
     * @param solicitud Solicitud para obtener el número de expediente
     * @param documentos Lista de documentos que se van a clasificar
     * 
     * @return true si se pudieron clasificar todos los documentos
     */
    @Override
    public void clasificarDocumentos(SolicitudGenerica solicitud, List<Documento> documentos) throws GestorDocumentalServiceException {
        boolean todosClasificados = true;
        File dst = clasificadoPath;
        for(Documento documento : documentos){
            if(!documento.clasificado){
                File file = getFile(documento);
                boolean ok = move(file, dst);
                if(ok){
                    documento.clasificado = true;
                    documento.save();
                }else{
                    todosClasificados = false;
                }
            }
        }
        
        // Clasificación de los documentos que ya estaban subidos en otra solicitud
        for (Documento doc: solicitud.documentacion.documentos) {
			if ((doc.refAed != null) && (doc.refAed == true)) {
				if(!doc.clasificado){
	                File file = getFile(doc);
	                boolean ok = move(file, dst);
	                if(ok){
	                    doc.clasificado = true;
	                    doc.save();
	                }else{
	                    todosClasificados = false;
	                }
			      	doc.refAed = false;
					doc.save();
				}
			}
		}
        
        if(todosClasificados == false){
            throw new GestorDocumentalServiceException("No se pudieron clasificar todos los documentos");
        }
    }

    /**
     * Añade una firma a un documento
     * 
     * Esta implementación no guarda las firmas de los documentos
     */
    @Override
    public void agregarFirma(Documento documento, Firma firma) throws GestorDocumentalServiceException {
        //No se están guardando las firmas de los documentos
    }

    @Override
    public Firma getFirma(Documento documento) throws GestorDocumentalServiceException {
      //No se están guardando las firmas de los documentos
        return null;
    }

    @Override
    public List<Tramite> getTramites() throws GestorDocumentalServiceException {
    	// ------- CÓDIGOS DE REQUERIMIENTO -------
        TiposCodigoRequerimiento tipoCodReqdbObligatoriedad = new TiposCodigoRequerimiento();
    	tipoCodReqdbObligatoriedad.codigo = "CodigoReqObligatoriedad";
		tipoCodReqdbObligatoriedad.descripcion = "El documento es obligatorio";
		tipoCodReqdbObligatoriedad.descripcionCorta = "CRO";
		
		TiposCodigoRequerimiento tipoCodReqdbCorrupto = new TiposCodigoRequerimiento();
		tipoCodReqdbCorrupto.codigo = "CodigoReqCorrupto";
		tipoCodReqdbCorrupto.descripcion = "El documento es ilegible y/o está corrupto";
		tipoCodReqdbCorrupto.descripcionCorta = "CRC";
		
		TiposCodigoRequerimiento tipoCodReqdbIncompleto = new TiposCodigoRequerimiento();
		tipoCodReqdbIncompleto.codigo = "CodigoReqIncompleto";
		tipoCodReqdbIncompleto.descripcion = "Documento incompleto";
		tipoCodReqdbIncompleto.descripcionCorta = "CRI";
		
		TiposCodigoRequerimiento tipoCodReqdbFirma = new TiposCodigoRequerimiento();
		tipoCodReqdbFirma.codigo = "CodigoReqFirma";
		tipoCodReqdbFirma.descripcion = "El documento no está debidamente firmado";
		tipoCodReqdbFirma.descripcionCorta = "CRF";
		
		TiposCodigoRequerimiento tipoCodReqdbEspanol = new TiposCodigoRequerimiento();
		tipoCodReqdbEspanol.codigo = "CodigoReqEspanol";
		tipoCodReqdbEspanol.descripcion = "Se requiere nacionalidad española";
		tipoCodReqdbEspanol.descripcionCorta = "CRE";
		
		TiposCodigoRequerimiento tipoCodReqdbDNI = new TiposCodigoRequerimiento();
		tipoCodReqdbDNI.codigo = "CodigoReqDNI";
		tipoCodReqdbDNI.descripcion = "Falta fotocopia del DNI";
		tipoCodReqdbDNI.descripcionCorta = "CRDNI";
  	

		String uriDocumento = null;

    	// ------- TRAMITE 1: Solicitud ------- 

        Tramite tramiteSolicitud = new Tramite();
        tramiteSolicitud.nombre = "Solicitud";
        tramiteSolicitud.uri = "fs://solicitud";

        // fap.aed.tiposdocumentos.base      ***** ¿Base no necesaria?
        uriDocumento = tramiteSolicitud.setDocumentoEnTramite("Base solicitud", "fs://basesolicitud/v01", "UNICO"); 	
        tramiteSolicitud.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbEspanol, tipoCodReqdbFirma);    
    	// fap.aed.tiposdocumentos.solicitud
        uriDocumento = tramiteSolicitud.setDocumentoEnTramite("Solicitud", "fs://solicitud/v01", "UNICO");  
        tramiteSolicitud.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbEspanol, tipoCodReqdbFirma, tipoCodReqdbObligatoriedad);
        // fap.aed.tiposdocumentos.justificanteRegistroSolicitud
        uriDocumento = tramiteSolicitud.setDocumentoEnTramite("Justificante del registro", "fs://justificanteregistro/v01", "UNICO"); 	
        tramiteSolicitud.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
        // fap.aed.tiposdocumentos.aportacion.solicitud
        uriDocumento = tramiteSolicitud.setDocumentoEnTramite("Aportación de la solicitud", "fs://aportacionsolicitud/v01", "MULTIPLE"); 	
        tramiteSolicitud.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbFirma, tipoCodReqdbDNI);    
        // fap.aed.tiposdocumentos.aportacion.registro
        uriDocumento = tramiteSolicitud.setDocumentoEnTramite("Aportación del registro", "fs://aportacionregistro/v01", "MULTIPLE"); 	
        tramiteSolicitud.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbEspanol, tipoCodReqdbFirma);
	    // fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento   
	    uriDocumento = tramiteSolicitud.setDocumentoEnTramite("Firma y registro de funcionario habilitado", "fs://firmafuncionariohabilitado/v01", "UNICO"); 	
	    tramiteSolicitud.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
        // fap.baremacion.evaluacion.documento.solicitud
        uriDocumento = tramiteSolicitud.setDocumentoEnTramite("Baremación de la evaluación del documento", "fs://baremacionevaluaciondocumento/v01", "UNICO"); 	
        tramiteSolicitud.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbFirma);    

    	// ------------------------- TRAMITE 2: Alegación ------------------------- 
        Tramite tramiteAlegacion = new Tramite();
        tramiteAlegacion.nombre = "Alegación";
        tramiteAlegacion.uri = "fs://alegacion";
        // fap.aed.tiposdocumentos.alegacion
        uriDocumento = tramiteAlegacion.setDocumentoEnTramite("Alegación", "fs://alegacion/v01", "UNICO"); 	
        tramiteAlegacion.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbFirma);     
        // fap.aed.tiposdocumentos.justificanteRegistroAlegacion
        uriDocumento = tramiteAlegacion.setDocumentoEnTramite("Justificante del registro de la alegación", "fs://justificanteregistroalegacion/v01", "UNICO"); 	
        tramiteAlegacion.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
        // fap.tramitacion.prefijojustificantepdf.solicitud
        uriDocumento = tramiteAlegacion.setDocumentoEnTramite("Prefijo justificante pdf alegación", "fs://prefijojustificantepdfalegacion/v01", "UNICO"); 	
        tramiteAlegacion.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
 
        // ------------------------- TRAMITE 3: Desestimiento ------------------------- 


        TipoDocumento tipoBase = newTipoDocumento("Base", "fs://base/v01");
        tipoBase.cardinalidad = "UNICO";
        tipoBase.tramitePertenece = tramiteSolicitud.uri;
        tramiteSolicitud.documentos.add(tipoBase);
        newTiposCodigoRequerimiento(tipoCodReqdbCorrupto, tipoBase.uri, tramiteSolicitud.uri);
        newTiposCodigoRequerimiento(tipoCodReqdbEspanol, tipoBase.uri, tramiteSolicitud.uri);
        newTiposCodigoRequerimiento(tipoCodReqdbFirma, tipoBase.uri, tramiteSolicitud.uri);
        
        TipoDocumento tipoSolicitud = newTipoDocumento("Solicitud", "fs://solicitud/v01");
        tipoSolicitud.cardinalidad = "MULTIPLE";
        tipoSolicitud.tramitePertenece = tramiteSolicitud.uri;
        tramiteSolicitud.documentos.add(tipoSolicitud);
        newTiposCodigoRequerimiento(tipoCodReqdbCorrupto, tipoSolicitud.uri, tramiteSolicitud.uri);
        newTiposCodigoRequerimiento(tipoCodReqdbDNI, tipoSolicitud.uri, tramiteSolicitud.uri);
        
        TipoDocumento tipoJustificanteRegistro = newTipoDocumento("JustificanteRegistro", "fs://justificanteRegistro/v01");
        tipoJustificanteRegistro.cardinalidad = "UNICO";
        tipoJustificanteRegistro.tramitePertenece = tramiteSolicitud.uri;
        tramiteSolicitud.documentos.add(tipoJustificanteRegistro);
        newTiposCodigoRequerimiento(tipoCodReqdbFirma, tipoJustificanteRegistro.uri, tramiteSolicitud.uri);
        
        TipoDocumento tipoficheroPeticion = newTipoDocumento("Fichero Petición", "fs://ficheroPeticion/v01");
        tipoficheroPeticion.cardinalidad = "UNICO";
        tipoficheroPeticion.tramitePertenece = tramiteSolicitud.uri;
        tramiteSolicitud.documentos.add(tipoficheroPeticion);
        newTiposCodigoRequerimiento(tipoCodReqdbFirma, tipoficheroPeticion.uri, tramiteSolicitud.uri);
        
        TipoDocumento tipoficheroRespuesta = newTipoDocumento("Fichero Respuesta", "fs://ficheroRespuesta/v01");
        tipoficheroRespuesta.cardinalidad = "UNICO";
        tipoficheroRespuesta.tramitePertenece = tramiteSolicitud.uri;
        tramiteSolicitud.documentos.add(tipoficheroRespuesta);
        newTiposCodigoRequerimiento(tipoCodReqdbFirma, tipoficheroRespuesta.uri, tramiteSolicitud.uri);
        
        // ------- TRAMITE 2: Aportación ------- 
        Tramite tramiteAportacion = new Tramite();
        tramiteAportacion.nombre = "aportacion";
        tramiteAportacion.uri = "fs://aportacion";

        TipoDocumento tipoSolicitudAport = newTipoDocumento("SolicitudAportacion", "fs://solicitudaportacion/v02");
        tipoSolicitudAport.cardinalidad = "UNICO";
        tipoSolicitudAport.tramitePertenece = tramiteAportacion.uri;
        tramiteAportacion.documentos.add(tipoSolicitudAport);
        newTiposCodigoRequerimiento(tipoCodReqdbIncompleto, tipoSolicitudAport.uri, tramiteAportacion.uri);
        newTiposCodigoRequerimiento(tipoCodReqdbObligatoriedad, tipoSolicitudAport.uri, tramiteAportacion.uri);
        
        TipoDocumento tipoAportRegistro = newTipoDocumento("AportacionRegistro", "fs://aportacionregistro/v01");
        tipoAportRegistro.cardinalidad = "UNICO";
        tipoAportRegistro.tramitePertenece = tramiteAportacion.uri;
        tramiteAportacion.documentos.add(tipoAportRegistro);
        newTiposCodigoRequerimiento(tipoCodReqdbFirma, tipoAportRegistro.uri, tramiteAportacion.uri);
        
        // ------- TRAMITE 3: Desestimiento ------- 

        Tramite tramiteDesestimiento = new Tramite();
        tramiteDesestimiento.nombre = "Desestimiento";
        tramiteDesestimiento.uri = "fs://desestimiento";
        // fap.aed.tiposdocumentos.desistimiento
        uriDocumento = tramiteDesestimiento.setDocumentoEnTramite("Desestimiento", "fs://desestimiento/v01", "UNICO"); 	
        tramiteDesestimiento.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbFirma, tipoCodReqdbIncompleto);    
        // fap.aed.tiposdocumentos.justificanteRegistroSolicitud
        uriDocumento = tramiteDesestimiento.setDocumentoEnTramite("Justificante del registro", "fs://justificanteregistrodesestimiento/v01", "UNICO"); 	
        tramiteDesestimiento.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
        // fap.tramitacion.prefijojustificantepdf.solicitud
        uriDocumento = tramiteDesestimiento.setDocumentoEnTramite("Prefijo justificante pdf desestimiento", "fs://prefijojustificantepdfdesestimiento/v01", "UNICO"); 	
        tramiteDesestimiento.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
        
        // ------------------------- TRAMITE 4: AceptacionRenuncia -------------------------
        Tramite tramiteAceptacionRenuncia = new Tramite();
        tramiteAceptacionRenuncia.nombre = "AceptacionRenuncia";
        tramiteAceptacionRenuncia.uri = "fs://aceptacionrenuncia";
        // fap.aed.tiposdocumentos.aceptacionrenuncia.aceptacion
        uriDocumento = tramiteAceptacionRenuncia.setDocumentoEnTramite("Aceptación de la solicitud", "fs://aceptacionsolicitud/v01", "UNICO"); 	
        tramiteAceptacionRenuncia.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
        // fap.aed.tiposdocumentos.aceptacionrenuncia.renuncia
        uriDocumento = tramiteAceptacionRenuncia.setDocumentoEnTramite("Renuncia de la solicitud", "fs://renunciasolicitud/v01", "UNICO"); 	
        tramiteAceptacionRenuncia.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto, tipoCodReqdbFirma);    
        // fap.aed.tiposdocumentos.justificanteRegistroAceptacionRenuncia.aceptacion
        uriDocumento = tramiteAceptacionRenuncia.setDocumentoEnTramite("Justificación del registro de la aceptación", "fs://justificanteregistroaceptacion/v01", "UNICO"); 	
        tramiteAceptacionRenuncia.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    
        // fap.aed.tiposdocumentos.justificanteRegistroAceptacionRenuncia.renuncia
        uriDocumento = tramiteAceptacionRenuncia.setDocumentoEnTramite("Justificación del registro de la renuncia", "fs://justificanteregistrorenuncia/v01", "UNICO"); 	
        tramiteAceptacionRenuncia.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);     
        // XXX: ¿Faltaría fap.aed.tiposdocumentos.justificanteRegistroSolicitud?
        // fap.tramitacion.prefijojustificantepdf.solicitud
        uriDocumento = tramiteAceptacionRenuncia.setDocumentoEnTramite("Prefijo justificante pdf desestimiento", "fs://prefijojustificantepdfaceptacionrenuncia/v01", "UNICO"); 	
        tramiteAceptacionRenuncia.setCodigosRequerimiento(uriDocumento, tipoCodReqdbCorrupto);    

        ArrayList<Tramite> tramites = new ArrayList<Tramite>();
        tramites.add(tramiteSolicitud);
        tramites.add(tramiteAlegacion);
        tramites.add(tramiteAportacion);
        tramites.add(tramiteDesestimiento);
        tramites.add(tramiteAceptacionRenuncia);
        return tramites;
    }
    
//    private TipoDocumento newTipoDocumento(String nombre, String tipo){
//        TipoDocumento tipoDocumento = new TipoDocumento();
//        tipoDocumento.nombre = "FileSystem " + nombre;
//        tipoDocumento.uri = tipo;
//        tipoDocumento.aportadoPor = "CIUDADANO";
//        tipoDocumento.obligatoriedad = "OBLIGATORIO";
//        return tipoDocumento;
//    }
	
    @Override
    public void actualizarCodigosExclusion() {
    	TipoCodigoExclusion tce = new TipoCodigoExclusion();
    	tce.codigo="0001";
    	tce.descripcion="Descripción Larga, Larguisima 1";
    	tce.descripcionCorta="Descripcion Corta 1";
    	tce.save();
    	tce = new TipoCodigoExclusion();
    	tce.codigo="0002";
    	tce.descripcion="Descripción Larga, Larguisima 2";
    	tce.descripcionCorta="Descripcion Corta 2";
    	tce.save();
    	tce = new TipoCodigoExclusion();
    	tce.codigo="0003";
    	tce.descripcion="Descripción Larga, Larguisima 3";
    	tce.descripcionCorta="Descripcion Corta 3";
    	tce.save();
    }
    
    private TipoDocumento newTipoDocumento(String nombre, String tipo){
        TipoDocumento tipoDocumento = new TipoDocumento();
        //tipoDocumento.nombre = "FileSystem " + nombre;
        tipoDocumento.nombre = nombre;
        tipoDocumento.uri = tipo;
        tipoDocumento.aportadoPor = "CIUDADANO";
        tipoDocumento.obligatoriedad = "OBLIGATORIO";
        return tipoDocumento;
    }

	private TiposCodigoRequerimiento newTiposCodigoRequerimiento(TiposCodigoRequerimiento codigoReq, 
																	String uriTipoDocumento, String uriTramite) {
		TiposCodigoRequerimiento tipoCodReqdb = new TiposCodigoRequerimiento();
		tipoCodReqdb.codigo = codigoReq.codigo;
		tipoCodReqdb.descripcion = codigoReq.descripcion;
		tipoCodReqdb.descripcionCorta = codigoReq.descripcionCorta;
		tipoCodReqdb.uriTipoDocumento = uriTipoDocumento;
		tipoCodReqdb.uriTramite = uriTramite;
		tipoCodReqdb.save();
		return tipoCodReqdb;
	}
	
	@Override
	public String crearExpediente(ExpedienteAed expedienteAed)
			throws GestorDocumentalServiceException {
        String expediente = expedienteAed.asignarIdAed();
        File folder = getExpedienteFolder(expediente);
        try {
            FileUtils.forceMkdir(folder);
        }catch(IOException e){
            throw new GestorDocumentalServiceException("Error al crear la carpeta " + folder.getAbsolutePath());
        }
        return expediente;
	}

	@Override
	public String modificarInteresados(ExpedienteAed expedienteAed,
			SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
        if(solicitud.solicitante == null)
            throw new NullPointerException();
        
        if(solicitud.solicitante.isPersonaFisica() && solicitud.solicitante.representado != null && solicitud.solicitante.representado){
            throw new NullPointerException();
        }
        
        if(solicitud.solicitante.isPersonaJuridica() && solicitud.solicitante.representantes == null){
            throw new NullPointerException();
        }
		String expediente = expedienteAed.asignarIdAed();
		File folder = getExpedienteFolder(expediente);
		if (!folder.exists() || (expediente.trim().equals("")))
			throw new GestorDocumentalServiceException("Error modificando expediente para el expediente (id: " + expedienteAed.id+"): No existe la carpeta o no tiene idAed.");
		return expediente;
	}
	
	public List<TipoDocumentoEnTramite> getTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
		String consulta = "select tipoDoc from TipoDocumento tipoDoc where (tipoDoc.aportadoPor = ? and tipoDoc.tramitePertenece = ?) ";
		List <TipoDocumentoEnTramite> tdtList = Documento.find(consulta, "CIUDADANO", tramite.uri).fetch();
		return tdtList;
	}
	
	public List<es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento> getListTiposDocumentosAportadosCiudadano(models.Tramite tramite) {
		List <es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento> tdList = 
				new ArrayList<es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento>();
		es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento td = 
				new es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento();
		
		td = newTipoDocumento("fs://dni/v01", "Fotocopia del DNI", "nif");
		tdList.add(td);
		td = newTipoDocumento("fs://justificanteregistro/v01", "Justificante del registro", "justificanteregistro");
		tdList.add(td);
		es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento td2 = new es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento();
		td2.setUri("fs://solicitud/v01");
		td2.setDescripcion("FileSystem FileSystem Solicitud");
		td2.setVersion(1);
		td2.setEtiqueta("Etiqueta Solicitud");
		tdList.add(td2);
		return tdList;
	}

	private es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento newTipoDocumento(
														String uri, String descripcion, String etiqueta) {
		es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento td = 
				new es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento();
		td.setUri(uri);
		td.setDescripcion(descripcion);
		td.setVersion(1);
		td.setEtiqueta(etiqueta);
		return td;
	}

	@Override
	public String getExpReg(){
		String expresionRegular="";
		return expresionRegular;
	}

	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento, SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
		duplicarDocumentoSubido(uriDocumento);
	}
	
	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, models.Documento dbDocumento, SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
		duplicarDocumentoSubido(uriDocumento, descripcionDocumento, dbDocumento);
	}
	
	/*
	 * Al subir un documento, se da la posibilidad de seleccionar uno ya subido previamente (y clasificado). 
	 * Esta función marca en ese documento (campos documento.RefAed y documento.expedienteReferenciado) que 
	 * debe estar en el expediente correspondiente. En el proceso de clasificación es cuando realmente este
	 * documento pasa a formar parte a todos los efectos del expediente.
	 * 
	 */
	public void duplicarDocumentoSubido(String uriDocumento) throws GestorDocumentalServiceException  {
		Documento documento = Documento.findByUri(uriDocumento);
		Documento doc = new Documento();
		doc.duplicar(documento);
		// El campo refAed se creó para verificar si el campo expedienteReferenciado/solicitudReferenciada es válido
		// Ahora lo ponemos true para que en saveDocumentoTemporal no compruebe que doc esté en el aed (por haber hecho un duplicar documento)
		doc.refAed = true;
		doc.fechaSubida = new DateTime();
		doc.save();
	}
	
	public void duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, Documento dbDocumento) throws GestorDocumentalServiceException  {
		Documento documento = Documento.findByUri(uriDocumento);
		dbDocumento.duplicar(documento);
		dbDocumento.descripcion = descripcionDocumento;
		// El campo refAed se creó para verificar si el campo expedienteReferenciado/solicitudReferenciada es válido
		// Ahora lo ponemos true para que en saveDocumentoTemporal no compruebe que doc esté en el aed (por haber hecho un duplicar documento)
		dbDocumento.refAed = true;
		dbDocumento.fechaSubida = new DateTime();
		dbDocumento.save();
	}

	@Override
	public void clasificarDocumentos(SolicitudGenerica solicitud,
			List<Documento> documentos,
			InformacionRegistro informacionRegistro, boolean notificable)
			throws GestorDocumentalServiceException {
		clasificarDocumentos(solicitud, documentos);
		
	}

	@Override
	public void clasificarDocumentos(SolicitudGenerica solicitud,
			List<Documento> documentos, boolean notificable)
			throws GestorDocumentalServiceException {
		clasificarDocumentos(solicitud, documentos);	
	}

	@Override
	public BinaryResponse getDocumentoConInformeDeFirma(Documento documento) throws GestorDocumentalServiceException {
		return getDocumento(documento);
	}

	@Override
	public BinaryResponse getDocumentoConInformeDeFirmaByUri(String uriDocumento) throws GestorDocumentalServiceException {
		return getDocumentoByUri(uriDocumento);
	}

	@Override
	public void agregarFirma(Documento documento, String firmaStr)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	public void setMetadatosDocumento(String uriDocumento) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String construyeIdentificador(String uriDocumento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime construyeFechaCaptura(String uriDocumento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String construyeEstadoElaboracion(String uriDocumento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String construyeNombreFormato(String uriDocumento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String construyeTipoFirmasElectronicas(String uriDocumento)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		return null;
	}
}
