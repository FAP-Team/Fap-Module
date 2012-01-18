package aed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;

import models.Firmante;
import models.InformacionRegistro;
import models.RepresentantePersonaJuridica;
import models.SolicitudGenerica;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import platino.PlatinoProxy;
import play.libs.MimeTypes;
import properties.FapProperties;
import utils.BinaryResponse;
import utils.StreamUtils;

import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.aed.ws.dominio.DocumentoEnUbicacion;
import es.gobcan.eadmon.aed.ws.dominio.Expediente;
import es.gobcan.eadmon.aed.ws.dominio.Ubicaciones;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Contenido;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Documento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.RegistroDocumento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.TipoPropiedadAvanzadaEnum;

/**
 * @deprecated Utilizar AedService con la nueva forma de inyectar dependencias
 */
@Deprecated
public class AedClient {

	private static AedPortType aed;
	
	private static Logger log = Logger.getLogger(AedClient.class);
	
	static {		
		URL wsdlURL = Aed.class.getClassLoader().getResource ("aed/aed.wsdl");
		aed = new Aed(wsdlURL).getAed(new MTOMFeature());
		
		BindingProvider bp = (BindingProvider) aed;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, FapProperties.get("fap.aed.url"));		
	
		PlatinoProxy.setProxy(aed);
	}
	
	public static String getVersion() throws AedExcepcion{
		Holder<String> version = new Holder<String>();
		Holder<String> revision = new Holder<String>();
		aed.obtenerVersionServicio(version, revision);
		return version.value;
	}
	
	
	public static String saveDocumentoTemporal(models.Documento documento, InputStream is, String filename) throws AedExcepcion {
		//Preparamos el documento para subir al AED
		documento.prepararParaSubir();
		
		Documento documentoAed = new Documento();
		// Propiedades básicas
		documentoAed.setPropiedades(new PropiedadesDocumento());
		documentoAed.getPropiedades().setDescripcion(documento.descripcion);
		documentoAed.getPropiedades().setUriTipoDocumento(documento.tipo);

		// Propiedades avanzadas
		
		documentoAed.getPropiedades().setTipoPropiedadesAvanzadas(TipoPropiedadAvanzadaEnum.ADMINISTRATIVO);
		PropiedadesAdministrativas propiedadesAdministrativas = new PropiedadesAdministrativas();
		documentoAed.getPropiedades().setPropiedadesAvanzadas(propiedadesAdministrativas);
		propiedadesAdministrativas.getInteresados().add(FapProperties.get("fap.aed.documentonoclasificado.interesado.nombre"));
		propiedadesAdministrativas.getInteresadosNombre().add(FapProperties.get("fap.aed.documentonoclasificado.interesado.nif"));
		
		// Contenido
		Contenido contenido = new Contenido();
		contenido.setNombre(filename);
		String mime = MimeTypes.getMimeType(filename, "application/octet-stream");
		contenido.setFichero(getDataHandler(is, mime));
		contenido.setTipoMime(mime);		
		documentoAed.setContenido(contenido);

		String ruta = FapProperties.get("fap.aed.temporales");
		String uriDocumentoTemporal = aed.crearDocumentoNoClasificado(ruta, documentoAed);
		
		documento.uri = uriDocumentoTemporal;
		documento.fechaSubida = new DateTime();
		documento.clasificado = false;
		
		//TXU
		PropiedadesDocumento pro  = aed.obtenerDocumentoPropiedadesNoClasificado(uriDocumentoTemporal);
		String hashAux = ((PropiedadesAdministrativas)pro.getPropiedadesAvanzadas()).getSellado().getHash();
		documento.hash=hashAux;
		log.debug("\r\n------ El hash= " + hashAux + " ------------\r\n");
		//System.out.println("\r\n------ El hash= " + hashAux + " ------------\r\n");
		//TXU
				
		documento.save();
		
		
		log.debug("Documento temporal creado uri=" + uriDocumentoTemporal);

		return uriDocumentoTemporal;		
	}
	
	public static String saveDocumentoTemporal(models.Documento documento, File file) throws AedExcepcion {
		try {
			log.info("Documento "+documento.toString());
			return saveDocumentoTemporal(documento, new FileInputStream(file), file.getName());
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Archivo no encontrado");
		}
	}
	
	private static DataHandler getDataHandler(File file) {
		DataSource dataSource = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(dataSource);
		return dataHandler;
	}
	
	private static DataHandler getDataHandler(InputStream inputStream, String mimetype) {
		DataSource dataSource = new InputStreamDataSource(inputStream, mimetype);
		DataHandler dataHandler = new DataHandler(dataSource);
		return dataHandler;
	}
	
	public static Boolean isClasificado(String uri){
		models.Documento docdb = models.Documento.find("byUri", uri).first();
		if(docdb == null){
			log.error("Documento no encontrado en la base de datos uri = " + uri);
			return null;
		}
		return docdb.clasificado;
	}
	
	public static BinaryResponse obtenerDoc(String uri) throws AedExcepcion, IOException {
		log.info("Obteniendo el documento con uri = " + uri);
		Boolean clasificado = isClasificado(uri);
		if(clasificado == null) return null;
		
		BinaryResponse response = new BinaryResponse();
		try {
			Documento doc;
			if (!clasificado)
				doc = aed.obtenerDocumentoNoClasificado(uri);
			else
				doc = aed.obtenerDocumento(uri);
			
			response.contenido = doc.getContenido().getFichero();
			response.nombre = doc.getContenido().getNombre();
		} catch (AedExcepcion e) {
			log.error("No se ha podido cargar el documento " +  uri + " clasificado= " + clasificado);
			throw e;
		}
		log.debug("Documento recuperado del aed " + uri);
		return response;
	}

	public static byte[] obtenerDocBytes(String uri) throws AedExcepcion, IOException{
		BinaryResponse doc = obtenerDoc(uri);
		InputStream is = doc.contenido.getInputStream();
        return StreamUtils.is2byteArray(is);
	}
	
	/**
	 * Devuelve las propiedades de un documento. El documento debe estar en la base de datos apra comprobar
	 * si está clasificado o no
	 * @param uri
	 * @return
	 * @throws AedExcepcion
	 */
	public static PropiedadesDocumento obtenerPropiedades(String uri) throws AedExcepcion {
		Boolean clasificado = isClasificado(uri);
		if(clasificado == null) return null;
		return obtenerPropiedades(uri, clasificado);
	}
	
	/**
	 * Devuelve las propiedades de un documento.
	 * @param uri
	 * @param clasificado
	 * @return
	 * @throws AedExcepcion
	 */
	public static PropiedadesDocumento obtenerPropiedades(String uri, Boolean clasificado) throws AedExcepcion {
		PropiedadesDocumento propiedades;
		if(clasificado){
			propiedades = aed.obtenerDocumentoPropiedades(uri);
		}else{
			propiedades = aed.obtenerDocumentoPropiedadesNoClasificado(uri);
		}
		return propiedades;
		
	}
	
	public static PropiedadesAdministrativas obtenerPropiedadesAdministrativas(String uri) throws AedExcepcion {
		return (PropiedadesAdministrativas) obtenerPropiedades(uri).getPropiedadesAvanzadas();
	}
	
	/**
	 * Actualiza el tipo y las descripción en el aed de un documento
	 * @param documento
	 * @throws AedExcepcion
	 */
	public static void actualizarTipoDescripcion(models.Documento documento) throws AedExcepcion {
		if(documento.uri == null) throw new IllegalArgumentException("La uri del documneto no puede ser null");
		documento.prepararParaSubir();
		
		if (documento.clasificado != null && documento.clasificado.booleanValue()) {
			log.debug("Actualizando tipo y descripción de un documento clasificado");
			log.debug("Obteniendo propiedades");
			PropiedadesDocumento props = aed.obtenerDocumentoPropiedades(documento.uri);
			
			log.debug("Obteniendo ubicaciones");
			List<DocumentoEnUbicacion> ubicaciones = aed.obtenerDocumentoRutas(documento.uri);
			if (ubicaciones.size() == 0) {
				log.error("No se pudieron obtener las ubicaciones del documento " + documento.uri);
				throw new AedExcepcion();
			}
			
			List<Ubicaciones> newUbicaciones = new ArrayList<Ubicaciones>();
			for (DocumentoEnUbicacion docUbic :ubicaciones) {
				Ubicaciones ubic = new Ubicaciones();
				ubic.setProcedimiento(FapProperties.get(""));
				ubic.getExpedientes().add(docUbic.getExpediente());
				newUbicaciones.add(ubic);
			}
			props.setDescripcion(documento.descripcion);
			props.setUriTipoDocumento(documento.tipo);
			
			log.debug("Actualizando Propiedades Clasificado");
			aed.actualizarDocumentoPropiedades(props, newUbicaciones);
		}else{
			log.info("Actualizando tipo y descripción de un documento no clasificado");
			log.debug("Obteniendo propiedades");
			PropiedadesDocumento props = aed.obtenerDocumentoPropiedadesNoClasificado(documento.uri);
			props.setDescripcion(documento.descripcion);
			props.setUriTipoDocumento(documento.tipo);
			log.debug("Actualizando PropiedadesNoClasificado");
			aed.actualizarDocumentoPropiedadesNoClasificado(props);
		}
	}
	
	private static String crearExpediente(String idExpediente, List<String> interesadoNif, List<String> interesadoNombre) throws AedExcepcion {
		String procedimiento = FapProperties.get("fap.aed.procedimiento");
		String convocatoria = FapProperties.get("fap.aed.convocatoria");
		
		Expediente expediente = new Expediente();
		expediente.setIdExterno(idExpediente);
		expediente.setProcedimiento(procedimiento);
		expediente.setValorModalidad(convocatoria);
		for (int i = 0; i < interesadoNif.size(); i++) {
			expediente.getInteresados().add(interesadoNif.get(i));
			expediente.getInteresadosNombre().add(interesadoNombre.get(i));
		}
		aed.crearExpediente(expediente);
		log.info("Expediente creado con id: " + idExpediente);
		return expediente.getIdExterno();
	}
	
	/**
	 * Crea el expediente en el archivo electrónico para la solicitud
	 * Si no tiene asignado ningún ID de expediente, obtiene uno nuevo
	 * Asigna como interesados el solicitante
	 * @param solicitud
	 */
	public static void crearExpediente(SolicitudGenerica solicitud){
		List<String> interesadosDocumentos = new ArrayList<String>();
		List<String> interesadosNombres = new ArrayList<String>();
		
		asignarInteresados(solicitud, interesadosDocumentos, interesadosNombres);
		
		//Obtiene un ID de expediente nuevo
		if(solicitud.expedienteAed.idAed == null){				
			solicitud.expedienteAed.asignarIdAed();
		}
		
		try {
			crearExpediente(solicitud.expedienteAed.idAed, interesadosDocumentos, interesadosNombres);
		}catch(AedExcepcion e){
			log.warn("El expediente del AED con id " + solicitud.expedienteAed.idAed + " ya estaba creado");
		}
	}
	
	/**
	 * Rellena lista con los nombres y los nip/cif de los interesados
	 * Dos opciones:
	 * 		- Solicitante persona física: Nombre y NIP del solicitante y del representante ( si lo hay )
	 *      - Solicitante persona jurídica: Nombre y Cif de la empresa y Nombre y NIP de los representantes
	 * @param solicitud
	 * @param documentos Lista que se rellenará con los nip/cif
	 * @param nombres    Lista que se rellenará con los nombres de los interesados
	 */
	public static void asignarInteresados(SolicitudGenerica solicitud, List<String> documentos, List<String> nombres){
		if(solicitud.solicitante.isPersonaFisica()){
			documentos.add(solicitud.solicitante.fisica.nip.valor);
			nombres.add(solicitud.solicitante.fisica.getNombreCompleto());
			if (solicitud.solicitante.representado) {
				documentos.add(solicitud.solicitante.representante.getNumeroId());
				nombres.add(solicitud.solicitante.representante.getNombreCompleto());
			}
		}else if(solicitud.solicitante.isPersonaJuridica()){
			documentos.add(solicitud.solicitante.juridica.cif);
			nombres.add(solicitud.solicitante.juridica.entidad);
			for(RepresentantePersonaJuridica representante: solicitud.solicitante.representantes){
				documentos.add(representante.getNumeroId());
				nombres.add(representante.getNombreCompleto());
			}
		}else{
			log.error("Obteniendo los interesados de la solicitud se encontró que el solicitante no es persona física ni jurídica");
		}
	}
	
	private static void clasificarDocumentoSinRegistro(String idAed, models.Documento documento, List<String> interesadosDocumentos, List<String> interesadosNombres) throws AedExcepcion{
		PropiedadesDocumento propiedades = obtenerPropiedades(documento.uri);
		clasificarDocumento(idAed, documento, propiedades, interesadosDocumentos, interesadosNombres);
	}

	private static void clasificarDocumentoConRegistro(String idAed, models.Documento documento, List<String> interesadosDocumentos, List<String> interesadosNombres, InformacionRegistro informacionRegistro, boolean notificable) throws AedExcepcion{
		PropiedadesDocumento propiedades = obtenerPropiedades(documento.uri);
		PropiedadesAdministrativas propAdmin = (PropiedadesAdministrativas) propiedades.getPropiedadesAvanzadas();
		
		//Asigna la información de registro
		RegistroDocumento registro = new RegistroDocumento();
		registro.setFechaRegistro(informacionRegistro.fechaRegistro.toDate());
		registro.setNumRegistro(informacionRegistro.numeroRegistro);
		registro.setNumRegistroGeneral(informacionRegistro.numeroRegistroGeneral);
		registro.setUnidadOrganica(informacionRegistro.unidadOrganica);
		propAdmin.setRegistro(registro);
		
		//Marca como notificable
		if(notificable)
			propAdmin.setNotificable(true);
		
		clasificarDocumento(idAed, documento, propiedades, interesadosDocumentos, interesadosNombres);
	}
	
	private static void clasificarDocumento(String idAed, models.Documento documento, PropiedadesDocumento propiedadesDocumento, List<String> interesadosDocumentos, List<String> interesadosNombre) throws AedExcepcion {
		// Registro de entrada
		PropiedadesAdministrativas propsAdmin = (PropiedadesAdministrativas)propiedadesDocumento.getPropiedadesAvanzadas();

		// Documentos pasan a ser del interesado no del user
		propsAdmin.getInteresados().clear();
		propsAdmin.getInteresados().addAll(interesadosDocumentos);
		propsAdmin.getInteresadosNombre().clear();
		propsAdmin.getInteresadosNombre().addAll(interesadosNombre);

		// Ubicaciones
		String procedimiento = FapProperties.get("fap.aed.procedimiento");
		List<Ubicaciones> ubicaciones = new ArrayList<Ubicaciones>();
		Ubicaciones ubicacionExpediente = new Ubicaciones();
		ubicacionExpediente.setProcedimiento(procedimiento);
		ubicacionExpediente.getExpedientes().add(idAed);
		ubicaciones.add(ubicacionExpediente);

		// Clasificar documento al expediente
		aed.clasificarDocumento(documento.uri, propiedadesDocumento, ubicaciones);
		documento.clasificado = true;
		documento.save();
		
		log.info("Documento temporal clasificado: Expediente: " + idAed + ", Documento: " + documento.uri);
	}
	
	/**
	 * Clasifica una lista de documentos
	 * Si la información de registro es null los marca como no registrados
	 * TODO: Pasar información de notificable
	 * @param documentos
	 * @param informacionRegistro
	 * @throws AedExcepcion 
	 */
	public static boolean clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, InformacionRegistro informacionRegistro){
		log.debug("Clasificando documentos");
		
		String idAed = solicitud.expedienteAed.idAed;
		List<String> interesadosNombres = new ArrayList<String>();
		List<String> interesadosDocumentos = new ArrayList<String>();
		asignarInteresados(solicitud, interesadosDocumentos, interesadosNombres);
		
		boolean todosClasificados = true;
		for(models.Documento documento : documentos){
			if(!documento.clasificado){
				try {
					if(informacionRegistro == null){
						clasificarDocumentoSinRegistro(idAed, documento, interesadosDocumentos, interesadosNombres);
					}else{
						//TODO: Pasar parámetro notificable
						clasificarDocumentoConRegistro(idAed, documento, interesadosDocumentos, interesadosNombres, informacionRegistro, false); 
					}
				}catch(AedExcepcion e){
					todosClasificados = false;
					log.error("Error al clasificar el documento " + documento.uri);
				}
			}else{
				log.warn("El documento " + documento.uri + " ya está clasificado");
			}
		}
		return todosClasificados;
	}
	
	
	/**
	 * Clasifica una lista de documentos no registrados
	 * @param solicitud
	 * @param documentos
	 * @throws AedExcepcion
	 */
	public static boolean clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos){
		return clasificarDocumentos(solicitud, documentos, null);
	}

	/**
	 * Añade una firma al documento
	 * 
	 * @param uri            Uri del documento
	 * @param firmante       Persona que firma
	 * @param firmaContenido Contenido de la firma
	 * @throws AedExcepcion
	 */
	public static void agregarFirma(String uri,  Firmante firmante, String firmaContenido) throws AedExcepcion {
		
		PropiedadesDocumento propiedadesDocumento = obtenerPropiedades(uri);
		PropiedadesAdministrativas propiedadesAdministrativas = (PropiedadesAdministrativas)propiedadesDocumento.getPropiedadesAvanzadas();
		Firma firma = null;
		
		if (propiedadesAdministrativas.getFirma() == null) {
			firma = new Firma();
			propiedadesAdministrativas.setFirma(firma);
			firma.setContenido(firmaContenido);
		} else {
			firma = propiedadesAdministrativas.getFirma();
			// No es la primera construimos firma paralela
			String firmaParalela = "<SignatureList>";
			String firmaOld = new String(firma.getContenido().getBytes());
			firmaOld = firmaOld.replaceFirst("<\\?.*\\?>", "");
			firmaOld = firmaOld.replaceFirst("<SignatureList>", "");
			firmaOld = firmaOld.replaceFirst("</SignatureList>", "");
			firmaParalela += firmaOld;
			firmaParalela += firmaContenido.replaceFirst("<\\?.*\\?>", "");
			firmaParalela += "</SignatureList>";
			firma.setContenido(firmaParalela);
		}
		firma.setTipoMime("text/xml");
		
		es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante firmanteAed = new es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante();
		firmanteAed.setFirmanteNombre(firmante.nombre);
		firmanteAed.setFirmanteNif(firmante.idvalor);
		firmanteAed.setFecha(firmante.fechaFirma.toDate());
		firma.getFirmantes().add(firmanteAed); // puede haber firmas anteriores

		actualizarPropiedades(propiedadesDocumento);
		log.info("Firma actualizada para el documento '" + uri + "' con el firmante " + firmante.idvalor);
	}
	
	public static void actualizarPropiedades(PropiedadesDocumento propiedades) throws AedExcepcion {
		String uri = propiedades.getUri();
		Boolean clasificado = isClasificado(uri);
		if(clasificado == null){
			log.error("El documento no se encuentra en la base de datos, no se puede comprobar si está clasificado o no");
		}else{
			actualizarPropiedades(propiedades, clasificado);
		}
	}
	
	public static void actualizarPropiedades(PropiedadesDocumento propiedades, Boolean clasificado) throws AedExcepcion {
		if(clasificado){
			//TODO falta ver las ubicaciones y si se incrementa la versión del documento
			//aed.actualizarDocumentoPropiedades(propiedades, arg1)
		}else{
			aed.actualizarDocumentoPropiedadesNoClasificado(propiedades);
		}
	}
	
	public static void borrarDocumento(models.Documento documento) throws AedExcepcion {
		if(documento == null || documento.uri == null){
			//Nothing to do here, bye
			return;
		}
		log.debug("Borrando documento con uri " + documento.uri);
		
		if(documento.clasificado){
			//No puedes borrar un documento clasificado
			log.info("Intentando borrar un documento ya clasificado, con uri = " + documento.uri);
			return;
		}
		
		aed.suprimirDocumentoNoClasificado(documento.uri);
		documento.delete();
		log.debug("Documento borrado");
	}
	
	public static List<String> obtenerUrisDocumentosEnExpediente(String expediente) throws AedExcepcion {
		List<PropiedadesDocumento> lista = aed.buscarDocumentos(FapProperties.get("fap.aed.procedimiento"), expediente, null, null, null, null, null, null, null);
		
		if (lista == null || lista.size() == 0) return null;
		
		List<String>  listOut = new ArrayList<String>();
		for(PropiedadesDocumento p : lista){
			listOut.add(p.getUri());
		}
		return listOut;
	}
	
}
