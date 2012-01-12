package services;

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
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;

import models.Firmante;
import models.InformacionRegistro;
import models.RepresentantePersonaJuridica;
import models.SolicitudGenerica;

import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import platino.PlatinoProxy;
import play.libs.MimeTypes;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import utils.BinaryResponse;
import utils.StreamUtils;
import utils.WSUtils;
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

public class AedServiceImpl implements AedService {

	private AedPortType aed;

	private final PropertyPlaceholder propertyPlaceholder;

	private static Logger log = Logger.getLogger(AedServiceImpl.class);
	
	public AedServiceImpl(PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
		URL wsdlURL = Aed.class.getClassLoader().getResource("aed/aed.wsdl");
		aed = new Aed(wsdlURL).getAed(new MTOMFeature());
		WSUtils.configureEndPoint(aed, getEndPoint());
		PlatinoProxy.setProxy(aed, propertyPlaceholder);
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#getPropertyPlaceholder()
	 */
	@Override
	public PropertyPlaceholder getPropertyPlaceholder(){
		return propertyPlaceholder;
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#getEndPoint()
	 */
	@Override
	public String getEndPoint(){
		return propertyPlaceholder.get("fap.aed.url");
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#getVersion()
	 */
	@Override
	public String getVersion() throws AedExcepcion {
		Holder<String> version = new Holder<String>();
		Holder<String> revision = new Holder<String>();
		aed.obtenerVersionServicio(version, revision);
		return version.value;
	}

	/* (non-Javadoc)
	 * @see services.AedService#getPort()
	 */
	@Override
	public AedPortType getPort() {
		return aed;
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#saveDocumentoTemporal(models.Documento, java.io.InputStream, java.lang.String)
	 */
	@Override
	public String saveDocumentoTemporal(models.Documento documento, InputStream is, String filename) throws AedExcepcion {
		//Preparamos el documento para subir al AED
		documento.prepararParaSubir();
		
		if(documento.tipo == null || documento.descripcion == null){
			throw new IllegalArgumentException("El tipo(" + documento.tipo + ") o la descripcion(" + documento.descripcion + ") del documento no pueden ser nulos");
		}
		
		Documento documentoAed = new Documento();
		// Propiedades básicas
		documentoAed.setPropiedades(new PropiedadesDocumento());
		documentoAed.getPropiedades().setDescripcion(documento.descripcion);
		documentoAed.getPropiedades().setUriTipoDocumento(documento.tipo);

		// Propiedades avanzadas
		documentoAed.getPropiedades().setTipoPropiedadesAvanzadas(TipoPropiedadAvanzadaEnum.ADMINISTRATIVO);
		PropiedadesAdministrativas propiedadesAdministrativas = new PropiedadesAdministrativas();
		documentoAed.getPropiedades().setPropiedadesAvanzadas(propiedadesAdministrativas);
		propiedadesAdministrativas.getInteresados().add(propertyPlaceholder.get("fap.aed.documentonoclasificado.interesado.nombre"));
		propiedadesAdministrativas.getInteresadosNombre().add(propertyPlaceholder.get("fap.aed.documentonoclasificado.interesado.nif"));
		
		// Contenido
		Contenido contenido = new Contenido();
		contenido.setNombre(filename);
		String mime = MimeTypes.getMimeType(filename, "application/octet-stream");
		contenido.setFichero(getDataHandler(is, mime));
		contenido.setTipoMime(mime);		
		documentoAed.setContenido(contenido);

		String ruta = propertyPlaceholder.get("fap.aed.temporales");
		String uriDocumentoTemporal = aed.crearDocumentoNoClasificado(ruta, documentoAed);
		
		documento.uri = uriDocumentoTemporal;
		documento.fechaSubida = new DateTime();
		documento.clasificado = false;
		
		// Almacena el Hash del documento
		PropiedadesDocumento pro  = aed.obtenerDocumentoPropiedadesNoClasificado(uriDocumentoTemporal);
		String hashAux = ((PropiedadesAdministrativas)pro.getPropiedadesAvanzadas()).getSellado().getHash();
		documento.hash=hashAux;
		
		//Guarda el documento
		documento.save();
		log.debug("Documento temporal creado uri=" + uriDocumentoTemporal);

		return uriDocumentoTemporal;		
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#saveDocumentoTemporal(models.Documento, java.io.File)
	 */
	@Override
	public String saveDocumentoTemporal(models.Documento documento, File file) throws AedExcepcion {
		try {
			return saveDocumentoTemporal(documento, new FileInputStream(file), file.getName());
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Archivo no encontrado");
		}
	}
	
	private DataHandler getDataHandler(InputStream inputStream, String mimetype) {
		DataSource dataSource = new InputStreamDataSource(inputStream, mimetype);
		DataHandler dataHandler = new DataHandler(dataSource);
		return dataHandler;
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#isClasificado(java.lang.String)
	 */
	@Override
	public Boolean isClasificado(String uri){
		models.Documento docdb = models.Documento.find("byUri", uri).first();
		if(docdb == null){
			log.error("Documento no encontrado en la base de datos uri = " + uri);
			return null;
		}
		return docdb.clasificado;
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#obtenerDoc(java.lang.String)
	 */
	@Override
	public BinaryResponse obtenerDoc(String uri) throws AedExcepcion, IOException {
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

	/* (non-Javadoc)
	 * @see services.AedService#obtenerDocBytes(java.lang.String)
	 */
	@Override
	public byte[] obtenerDocBytes(String uri) throws AedExcepcion, IOException {
		byte[] result = null;
		BinaryResponse doc = obtenerDoc(uri);
		if(doc != null){
			InputStream is = doc.contenido.getInputStream();
			result = StreamUtils.is2byteArray(is);
		}
        return result;
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#obtenerPropiedades(java.lang.String)
	 */
	@Override
	public PropiedadesDocumento obtenerPropiedades(String uri) throws AedExcepcion {
		Boolean clasificado = isClasificado(uri);
		if(clasificado == null) return null;
		return obtenerPropiedades(uri, clasificado);
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#obtenerPropiedades(java.lang.String, java.lang.Boolean)
	 */
	@Override
	public PropiedadesDocumento obtenerPropiedades(String uri, Boolean clasificado) throws AedExcepcion {
		PropiedadesDocumento propiedades;
		if(clasificado){
			propiedades = aed.obtenerDocumentoPropiedades(uri);
		}else{
			propiedades = aed.obtenerDocumentoPropiedadesNoClasificado(uri);
		}
		return propiedades;
		
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#obtenerPropiedadesAdministrativas(java.lang.String)
	 */
	@Override
	public PropiedadesAdministrativas obtenerPropiedadesAdministrativas(String uri) throws AedExcepcion {
		return (PropiedadesAdministrativas) obtenerPropiedades(uri).getPropiedadesAvanzadas();
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#actualizarTipoDescripcion(models.Documento)
	 */
	@Override
	public void actualizarTipoDescripcion(models.Documento documento) throws AedExcepcion {
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
	
	private String crearExpediente(String idExpediente, List<String> interesadoNif, List<String> interesadoNombre) throws AedExcepcion {
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
	
	/* (non-Javadoc)
	 * @see services.AedService#crearExpediente(models.SolicitudGenerica)
	 */
	@Override
	public void crearExpediente(SolicitudGenerica solicitud){
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
	
	/* (non-Javadoc)
	 * @see services.AedService#asignarInteresados(models.SolicitudGenerica, java.util.List, java.util.List)
	 */
	@Override
	public void asignarInteresados(SolicitudGenerica solicitud, List<String> documentos, List<String> nombres){
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
	
	private void clasificarDocumentoSinRegistro(String idAed, models.Documento documento, List<String> interesadosDocumentos, List<String> interesadosNombres) throws AedExcepcion {
		PropiedadesDocumento propiedades = obtenerPropiedades(documento.uri);
		clasificarDocumento(idAed, documento, propiedades, interesadosDocumentos, interesadosNombres);
	}

	private void clasificarDocumentoConRegistro(String idAed, models.Documento documento, List<String> interesadosDocumentos, List<String> interesadosNombres, InformacionRegistro informacionRegistro, boolean notificable) throws AedExcepcion{
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
	
	private void clasificarDocumento(String idAed, models.Documento documento, PropiedadesDocumento propiedadesDocumento, List<String> interesadosDocumentos, List<String> interesadosNombre) throws AedExcepcion {
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
	
	/* (non-Javadoc)
	 * @see services.AedService#clasificarDocumentos(models.SolicitudGenerica, java.util.List, models.InformacionRegistro)
	 */
	@Override
	public boolean clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, InformacionRegistro informacionRegistro){
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
	
	/* (non-Javadoc)
	 * @see services.AedService#clasificarDocumentos(models.SolicitudGenerica, java.util.List)
	 */
	@Override
	public boolean clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos){
		return clasificarDocumentos(solicitud, documentos, null);
	}

	/* (non-Javadoc)
	 * @see services.AedService#agregarFirma(java.lang.String, models.Firmante, java.lang.String)
	 */
	@Override
	public void agregarFirma(String uri,  Firmante firmante, String firmaContenido) throws AedExcepcion {
		
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
	
	/* (non-Javadoc)
	 * @see services.AedService#actualizarPropiedades(es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento)
	 */
	@Override
	public void actualizarPropiedades(PropiedadesDocumento propiedades) throws AedExcepcion {
		String uri = propiedades.getUri();
		Boolean clasificado = isClasificado(uri);
		if(clasificado == null){
			log.error("El documento no se encuentra en la base de datos, no se puede comprobar si está clasificado o no");
		}else{
			actualizarPropiedades(propiedades, clasificado);
		}
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#actualizarPropiedades(es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento, java.lang.Boolean)
	 */
	@Override
	public void actualizarPropiedades(PropiedadesDocumento propiedades, Boolean clasificado) throws AedExcepcion {
		if(clasificado){
			//TODO falta ver las ubicaciones y si se incrementa la versión del documento
			//aed.actualizarDocumentoPropiedades(propiedades, arg1)
		}else{
			aed.actualizarDocumentoPropiedadesNoClasificado(propiedades);
		}
	}
	
	/* (non-Javadoc)
	 * @see services.AedService#borrarDocumento(models.Documento)
	 */
	@Override
	public void borrarDocumento(models.Documento documento) throws AedExcepcion {
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
	
	/* (non-Javadoc)
	 * @see services.AedService#obtenerUrisDocumentosEnExpediente(java.lang.String)
	 */
	@Override
	public List<String> obtenerUrisDocumentosEnExpediente(String expediente) throws AedExcepcion {
		List<PropiedadesDocumento> lista = aed.buscarDocumentos(FapProperties.get("fap.aed.procedimiento"), expediente, null, null, null, null, null, null, null);
		
		if (lista == null || lista.size() == 0) return null;
		
		List<String>  listOut = new ArrayList<String>();
		for(PropiedadesDocumento p : lista){
			listOut.add(p.getUri());
		}
		return listOut;
	}
	
}
