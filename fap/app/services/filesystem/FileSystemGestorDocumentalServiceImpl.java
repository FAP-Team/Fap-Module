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
	    
	     	// Documentos (de tipo tipoDocumento) de las solicitudes donde ha participado el agente actualmente logueado 
//	     	rows = Documento.find("select documento2 " +
//	     			"from Documentacion documentacion2 join documentacion2.documentos documento2 " +
//    				"where (documento2.tipo = '" + tipoDocumento + "') and  (documento2.clasificado = 1) and (documentacion2.id in " +
//	     				// Seleccionamos los id de Documentación pertenecientes a las solicitudes en los que el agente actualmente logueado ha participado
//	     				"(select documentacion.id from Documentacion documentacion, Solicitud solicitud " +
//	     				"where (solicitud.documentacion.id = documentacion.id) and " +
//	     				"(solicitud.id in " +
//	     						// Seleccionamos los id de las solicitudes en los que el agente actualmente logueado ha participado
//		     					"(select solicitud2.id from Participacion participacion, Solicitud solicitud2 " +
//		    					" where (participacion.agente.username = '" + agente.username + "') and " +
//		    						"(solicitud2.id = participacion.solicitud.id) )" +
//	     				")" +
//    				"))").fetch();
	    	
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

        documento.prepararParaSubir();
        
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
		documento.solicitudReferenciada = null;	
		documento.expedienteReferenciado = null;
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
        Tramite tramite = new Tramite();
        tramite.nombre = "solicitud";
        tramite.uri = "fs://";

        TipoDocumento tipo = newTipoDocumento("FileSystem1","fs://type1/v01");
        tipo.cardinalidad = "UNICO";
        tipo.tramitePertenece=tramite.uri;
        tramite.documentos.add(tipo);
        TiposCodigoRequerimiento tipoCodReqdb = new TiposCodigoRequerimiento();
		tipoCodReqdb.codigo = "CodigoReq1";
		tipoCodReqdb.descripcion = "Descripcion para el Código de Requerimiento 1 del tipo de documento FileSystem1";
		tipoCodReqdb.descripcionCorta = "CR1FS1";
		tipoCodReqdb.uriTipoDocumento = tipo.uri;
		tipoCodReqdb.uriTramite = tramite.uri;
		tipoCodReqdb.save();
		TiposCodigoRequerimiento tipoCodReqdb2 = new TiposCodigoRequerimiento();
		tipoCodReqdb2.codigo = "CodigoReq2";
		tipoCodReqdb2.descripcion = "Descripcion para el Código de Requerimiento 2 del tipo de documento FileSystem1";
		tipoCodReqdb2.descripcionCorta = "CR2FS1";
		tipoCodReqdb2.uriTipoDocumento = tipo.uri;
		tipoCodReqdb2.uriTramite = tramite.uri;
		tipoCodReqdb2.save();
        
		TipoDocumento tipo2 = newTipoDocumento("FileSystem2", "fs://type2/v01");
		tipo2.cardinalidad = "MULTIPLE";
		tipo2.tramitePertenece=tramite.uri;
        tramite.documentos.add(tipo2);
        TiposCodigoRequerimiento tipoCodReqdb3 = new TiposCodigoRequerimiento();
		tipoCodReqdb3.codigo = "CodigoReq1";
		tipoCodReqdb3.descripcion = "Descripcion para el Código de Requerimiento 1 del tipo de documento FileSystem2";
		tipoCodReqdb3.descripcionCorta = "CR1FS2";
		tipoCodReqdb3.uriTipoDocumento = tipo2.uri;
		tipoCodReqdb3.uriTramite = tramite.uri;
		tipoCodReqdb3.save();
		
		TipoDocumento tipo3 = newTipoDocumento("FileSystem3", "fs://type3/v01");
		tipo3.cardinalidad = "UNICO";
		tipo3.tramitePertenece=tramite.uri;
        tramite.documentos.add(tipo3);
        TiposCodigoRequerimiento tipoCodReqdb4 = new TiposCodigoRequerimiento();
		tipoCodReqdb4.codigo = "CodigoReq1";
		tipoCodReqdb4.descripcion = "Descripcion para el Código de Requerimiento 1 del tipo de documento FileSystem3";
		tipoCodReqdb4.descripcionCorta = "CR1FS3";
		tipoCodReqdb4.uriTipoDocumento = tipo3.uri;
		tipoCodReqdb4.uriTramite = tramite.uri;
		tipoCodReqdb4.save();
		TiposCodigoRequerimiento tipoCodReqdb5 = new TiposCodigoRequerimiento();
		tipoCodReqdb5.codigo = "CodigoReq2";
		tipoCodReqdb5.descripcion = "Descripcion para el Código de Requerimiento 2 del tipo de documento FileSystem3";
		tipoCodReqdb5.descripcionCorta = "CR2FS3";
		tipoCodReqdb5.uriTipoDocumento = tipo3.uri;
		tipoCodReqdb5.uriTramite = tramite.uri;
		tipoCodReqdb5.save();
		TiposCodigoRequerimiento tipoCodReqdb6 = new TiposCodigoRequerimiento();
		tipoCodReqdb6.codigo = "CodigoReq3";
		tipoCodReqdb6.descripcion = "Descripcion para el Código de Requerimiento 3 del tipo de documento FileSystem3";
		tipoCodReqdb6.descripcionCorta = "CR3FS3";
		tipoCodReqdb6.uriTipoDocumento = tipo3.uri;
		tipoCodReqdb6.uriTramite = tramite.uri;
		tipoCodReqdb6.save();
		
		TipoDocumento tipo4=newTipoDocumento("Otros", "fs://otros/v01");
		tipo4.cardinalidad = "MULTIPLE";
		tipo4.tramitePertenece=tramite.uri;
        tramite.documentos.add(tipo4);
        TiposCodigoRequerimiento tipoCodReqdb7 = new TiposCodigoRequerimiento();
		tipoCodReqdb7.codigo = "CodigoReq1";
		tipoCodReqdb7.descripcion = "Descripcion para el Código de Requerimiento 1 del tipo de documento Otros";
		tipoCodReqdb7.descripcionCorta = "CR1Otros";
		tipoCodReqdb7.uriTipoDocumento = tipo4.uri;
		tipoCodReqdb7.uriTramite = tramite.uri;
		tipoCodReqdb7.save();
        
        
        ArrayList<Tramite> tramites = new ArrayList<Tramite>();
        tramites.add(tramite);
        return tramites;
    }
    
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
        tipoDocumento.nombre = "FileSystem " + nombre;
        tipoDocumento.uri=tipo;
        tipoDocumento.aportadoPor = "Ciudadano";
        tipoDocumento.obligatoriedad = "Obligatorio";
        return tipoDocumento;
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
		List <TipoDocumentoEnTramite> tdtList = new ArrayList<TipoDocumentoEnTramite>();
		TipoDocumentoEnTramite tdt = new TipoDocumentoEnTramite();
		tdt.setAportadoPor(AportadoPorEnum.CIUDADANO);
		tdt.setCardinalidad(CardinalidadEnum.UNICO);
		tdt.setIdentificador("1");
		tdt.setObligatoriedad(ObligatoriedadEnum.OBLIGATORIO);
		tdt.setVersion(1);
		tdt.setUri("fs://type1/v01");
		tdtList.add(tdt);
		return tdtList;
	}
	
	public List<es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento> getListTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
		List <es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento> tdList = new ArrayList<es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento>();
		es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento td = new es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento();
		td.setUri("fs://type1/v01");
		td.setDescripcion("FileSystem FileSystem 1");
		td.setVersion(1);
		td.setEtiqueta("Etiqueta1");
		tdList.add(td);
		es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento td2 = new es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento();
		td2.setUri("fs://type2/v01");
		td2.setDescripcion("FileSystem FileSystem 2");
		td2.setVersion(1);
		td2.setEtiqueta("Etiqueta2");
		tdList.add(td2);
		return tdList;
	}
	
	@Override
	public String getExpReg(){
		String expresionRegular="";
		return expresionRegular;
	}

	
	
	/*
	 * Al subir un documento, se da la posibilidad de seleccionar uno ya subido previamente (y clasificado). 
	 * Esta función marca en ese documento (campos documento.RefAed y documento.expedienteReferenciado) que 
	 * debe estar en el expediente correspondiente. En el proceso de clasificación es cuando realmente este
	 * documento pasa a formar parte a todos los efectos del expediente.
	 * 
	 */
	public void duplicarDocumentoSubido(String uriDocumento, SolicitudGenerica solicitud) throws GestorDocumentalServiceException  {
		SolicitudGenerica solicitudReferenciada = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud " +
				 											"where ( solicitud.documentacion.id in (" +
				 												"select documentacion.id from Documentacion documentacion join documentacion.documentos doc " + 
				 												"where doc.uri = '" + uriDocumento + "') )").first();
		if (solicitudReferenciada == null) {
			throw new GestorDocumentalServiceException("No se encuentra la solicitud que debe tener el documento con uri " + uriDocumento + 
									" en la duplicación de un documento ya subido.");
		}
		
		Documento documento = Documento.findByUri(uriDocumento);
		Documento doc = new Documento();
		doc.duplicar(documento);
		// El campo refAed se creó para verificar si el campo expedienteReferenciado/solicitudReferenciada es válido
		// Ahora lo ponemos true para que en saveDocumentoTemporal no compruebe que doc esté en el aed (por haber hecho un duplicar documento)
		doc.refAed = true;			
		doc.save();	
		
		File contenidoOriginal = getFile(documento);
		FileInputStream contenido = null;
		try {
			contenido = new FileInputStream(contenidoOriginal);
		} catch (FileNotFoundException e) { System.out.println(e); }	

		saveDocumentoTemporal(doc, contenido, contenidoOriginal.getName());
		
		doc.refAed = true;
		doc.solicitudReferenciada = solicitudReferenciada.id;
        doc.fechaSubida = new DateTime();
		doc.save();
		
		solicitud.documentacion.documentos.add(doc);
		solicitud.save();
	}
}
