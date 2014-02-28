package services.platino;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.ivy.plugins.repository.sftp.SFTPRepository;
import org.apache.log4j.Logger;
import org.apache.log4j.pattern.PropertiesPatternConverter;
import org.joda.time.DateTime;

import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;

import platino.DatosDocumento;
import platino.KeystoreCallbackHandler;
import platino.PlatinoCXFSecurityHeaders;
import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import messages.Messages;
import models.Agente;
import models.Convocatoria;
import models.Documento;
import models.ExpedientePlatino;
import models.ResolucionFAP;
import models.SolicitudFirmaPortafirma;
import models.SolicitudPortafirmaFAP;

import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;
import services.platino.*;
import tags.ComboItem;
import utils.BinaryResponse;
import utils.WSUtils;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.DocumentoAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoDocumentoEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionServiceBean;
import es.gobcan.platino.servicios.portafirmas.dominio.*;
import es.gobcan.platino.servicios.portafirmas.servicio.solicitudfirma.*;
import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.*;

@InjectSupport
public class PlatinoPortafirmaServiceImpl implements PortafirmaFapService {
	
	private SolicitudFirmaInterface portafirmaPort;
	private PropertyPlaceholder propertyPlaceholder;
	private static Logger log = Logger.getLogger(PlatinoPortafirmaServiceImpl.class);
	
	//private String userID;
	
	private GestorDocumentalService gestorDocumentalPort;
	private PlatinoGestorDocumentalService platinoGestorDocumentalPort;
	private PlatinoProcedimientosServiceImpl platinoProcedimientosPort;
	private PlatinoBDOrganizacionServiceImpl platinoDBOrgPort;

	@Inject
	public PlatinoPortafirmaServiceImpl(PropertyPlaceholder propertyPlaceholder) {
		
		this.propertyPlaceholder = propertyPlaceholder;
		gestorDocumentalPort = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		platinoProcedimientosPort = InjectorConfig.getInjector().getInstance(PlatinoProcedimientosServiceImpl.class);
		platinoDBOrgPort = InjectorConfig.getInjector().getInstance(PlatinoBDOrganizacionServiceImpl.class);
		platinoGestorDocumentalPort = new PlatinoGestorDocumentalService(propertyPlaceholder);
		
		URL wsdlURL = PlatinoPortafirmaServiceImpl.class.getClassLoader().getResource("wsdl/portafirmas/solicitudfirma.wsdl");
		portafirmaPort = new SolicitudFirma(wsdlURL).getSolicitudFirmaSoapHttp();
		
		WSUtils.configureEndPoint(portafirmaPort, getEndPoint());
		WSUtils.configureSecurityHeaders(portafirmaPort, propertyPlaceholder);
		
		PlatinoProxy.setProxy(portafirmaPort, propertyPlaceholder);
        Client client = ClientProxy.getClient(portafirmaPort);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
	}
	
	//---------CONFIGURACION SERVICIO----------
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.portafirma.url");
	}
	
	public boolean isConfigured() {
	    return hasConnection();
	}
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = obtenerVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
	//END------CONFIGURACION SERVICIO-----------
	//------------------------------------------
	
	//---------TOOLS----------------------------
	
	public static XMLGregorianCalendar DateTime2XMLGregorianCalendar(DateTime fecha) throws DatatypeConfigurationException {
		if (fecha == null)
			return null;
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTime(fecha.toDate());
		gcal.setTimeInMillis(fecha.getMillis());
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}

	private void setupSecurityHeadersWithUser(String uid) {
		try {
			String userUri = platinoDBOrgPort.recuperarURIPersona(uid);
			WSUtils.configureSecurityHeadersWithUser(portafirmaPort, userUri);
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al configurar cabecera de seguridad para usuario: " + uid + ". " + e.getMessage());
			//throw new PortafirmaFapServiceException("Error al configurar cabecera de seguridad para usuario: " + uid + ". " + e.getMessage(), e);
		}
	}
	
	private void restoreSecurityHeadersBackoffice() {
		String backoffice = propertyPlaceholder.get("fap.platino.security.backoffice.uri");
		WSUtils.configureSecurityHeadersWithUser(portafirmaPort, backoffice);
	}
	
	private void subirDocumentoAEDaPlatino(Documento documento, String uriUsuario) {
		//Restauramos cabeceras de seguridad por si hace falta subir documentos a platino
		restoreSecurityHeadersBackoffice();
		
		try {
			//Subir documento a firmar a gestor documental de platino (si no está subido)
			if (documento.uriPlatino == null) {
				
				//Obtenemos la ruta del expediente (convertida a platino)
				ExpedientePlatino expedientePlatino = ExpedientePlatino.all().first();
				String uriPlatinoExpediente = platinoGestorDocumentalPort.convertToHexNoQuery(expedientePlatino.getRuta());
				
				//Obtenemos el documento original del gestor documental
				BinaryResponse doc = gestorDocumentalPort.getDocumentoByUri(documento.uri);
				
				//Configuramos los datos de subida del documento
				DatosDocumento datos = new DatosDocumento();
				datos.setContenido(doc.contenido.getDataSource());
				datos.setTipoMime(doc.contenido.getContentType());
				datos.setFecha(DateTime2XMLGregorianCalendar(DateTime.now()));
				datos.setDescripcion(documento.descripcionVisible);
				datos.setAdmiteVersionado(true);
				
				//Subimos el documento al gestor documental de platino
				documento.uriPlatino = platinoGestorDocumentalPort.guardarDocumento(uriPlatinoExpediente, datos);
				documento.save();	
			}
			
		} catch (PlatinoGestorDocumentalServiceException e) {
			e.printStackTrace();
			play.Logger.info("Error al acceder al gestor documental de platino: " + e.getMessage());
			//throw new SolicitudFirmaExcepcion("Error al acceder al gestor documental de platino: " + e.getMessage(), e);
			
		} catch (GestorDocumentalServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			setupSecurityHeadersWithUser(uriUsuario);
		}
	}

	private void asociarDocumento(String uriSolicitud, String idSolicitante, List<Documento> documentos, TipoDocumentacionEnumType tipoDocumentacionEnumType) throws SolicitudFirmaExcepcion {
		if (documentos != null) {
			for (Documento documento: documentos) {
				// Copiamos el documento a Platino si no está
				subirDocumentoAEDaPlatino(documento, idSolicitante);
				// Añadimos el documento a la solicitud de firma
				DocumentoSolicitudFirmaType documentoSolicitudFirmaType = new DocumentoSolicitudFirmaType();
				documentoSolicitudFirmaType.setUriDocumento(documento.uriPlatino);
				documentoSolicitudFirmaType.setComentario(documento.descripcionVisible); // TODO: Revisar si setComentario es lo que sale en la descripción del  interfaz de portafirma.
				play.Logger.info("Se adjunta documento " + documento.uriPlatino + " a la solicitud de firma " + uriSolicitud);
				// TODO: Comprobar si devuelve una uri o sólo un error cuando falla.
				portafirmaPort.crearDocumento(uriSolicitud, documentoSolicitudFirmaType, tipoDocumentacionEnumType);
			}
		}
	}

	//END------TOOLS----------------------------
	//------------------------------------------
	
	//---------IMPLEMENTACION INTERFAZ----------
	
	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Portafirma ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de Portafirma ha sido inyectado con Platino y NO está operativo.");
	}

	@Override
	public String obtenerVersion() throws PortafirmaFapServiceException {
		try {
			return portafirmaPort.getVersion();
		} catch (SolicitudFirmaExcepcion e) {
			e.printStackTrace();
		}
		return null;
	}

	// TODO 
	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		
		// Se rellena SolicitudFirmaPortafirma a partir de ResolucionFAP
		resolucion.solicitudFirmaPortafirma.tema = resolucion.tituloInterno;
		if ((resolucion.descripcion == null) || (resolucion.descripcion.trim().equals("")))
			resolucion.solicitudFirmaPortafirma.materia = resolucion.sintesis;
		else
			resolucion.solicitudFirmaPortafirma.materia = resolucion.descripcion;
		resolucion.solicitudFirmaPortafirma.documentosFirma.add(resolucion.registro.oficial);
		for (Documento documento: resolucion.docConsultaPortafirmasResolucion) {
			resolucion.solicitudFirmaPortafirma.documentosConsulta.add(documento);
		}
		// TODO: ¿Por qué se cogen de gestor documental?
		resolucion.solicitudFirmaPortafirma.procedimiento = properties.FapProperties.get("fap.platino.gestordocumental.procedimiento");
		resolucion.solicitudFirmaPortafirma.expediente = properties.FapProperties.get("fap.platino.gestordocumental.expediente.descripcion");
		resolucion.solicitudFirmaPortafirma.tipoDocumento = "SOL";
		resolucion.solicitudFirmaPortafirma.mecanismoFirma = "SERIE";
		resolucion.solicitudFirmaPortafirma.solicitudFechaInicio = new DateTime();
		resolucion.solicitudFirmaPortafirma.save();
		
		//Configuramos el usuario del agente actual, recuperando su URI de BDOrg de platino
		setupSecurityHeadersWithUser(resolucion.solicitudFirmaPortafirma.idSolicitante);
		
		try {
			// Crear solicitud de firma
			SolicitudFirmaPropiedadesType solicitudFirmaPropiedadesType = crearSolicitudFirmaPlatino(resolucion.solicitudFirmaPortafirma);
			resolucion.solicitudFirmaPortafirma.uriSolicitud = portafirmaPort.crearSolicitudFirma(solicitudFirmaPropiedadesType);
			resolucion.save();
			
			//Adjuntamos el documento a firmar y los documentos anexos
			asociarDocumento(resolucion.solicitudFirmaPortafirma.uriSolicitud, resolucion.solicitudFirmaPortafirma.idSolicitante, resolucion.solicitudFirmaPortafirma.documentosFirma, TipoDocumentacionEnumType.PRINCIPAL);
			asociarDocumento(resolucion.solicitudFirmaPortafirma.uriSolicitud, resolucion.solicitudFirmaPortafirma.idSolicitante, resolucion.solicitudFirmaPortafirma.documentosConsulta, TipoDocumentacionEnumType.ANEXA); 
			
			PortafirmaCrearSolicitudResponse portafirmaCrearSolicitudResponse = new PortafirmaCrearSolicitudResponse();
			portafirmaCrearSolicitudResponse.setIdSolicitud(resolucion.solicitudFirmaPortafirma.uriSolicitud);
			resolucion.solicitudFirmaPortafirma.solicitudEstadoComentario = "La solicitud de firma de portafirma ha sido enviada.";
			resolucion.save();
			portafirmaCrearSolicitudResponse.setComentarios(resolucion.solicitudFirmaPortafirma.solicitudEstadoComentario);
			return portafirmaCrearSolicitudResponse;
			
		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.info("Error al crear la solicitud de firma: " + e.getMessage());
			throw new PortafirmaFapServiceException("Error al crear la solicitud de firma: " + e.getMessage(), e);
			
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	private SolicitudFirmaPropiedadesType crearSolicitudFirmaPlatino (SolicitudFirmaPortafirma solicitudFirmaPortafirma) {
		SolicitudFirmaPropiedadesType solicitudFirmaPropiedadesType = new SolicitudFirmaPropiedadesType();
		solicitudFirmaPropiedadesType.setArchivada(solicitudFirmaPortafirma.archivada);
		if (solicitudFirmaPortafirma.expediente == null)
			solicitudFirmaPropiedadesType.setExpediente(properties.FapProperties.get("fap.platino.gestordocumental.expediente.descripcion"));
		else
			solicitudFirmaPropiedadesType.setExpediente(solicitudFirmaPortafirma.expediente);
		solicitudFirmaPropiedadesType.setFirmaDelegada(solicitudFirmaPortafirma.firmaDelegada);
		try {
			solicitudFirmaPropiedadesType.setFirmaFecha(DateTime2XMLGregorianCalendar(solicitudFirmaPortafirma.firmaFecha));
			solicitudFirmaPropiedadesType.setPlazoMaximo(DateTime2XMLGregorianCalendar(solicitudFirmaPortafirma.plazoMaximo));
			solicitudFirmaPropiedadesType.setSolicitudEstadoFecha(DateTime2XMLGregorianCalendar(solicitudFirmaPortafirma.solicitudEstadoFecha));
			solicitudFirmaPropiedadesType.setSolicitudFechaFin(DateTime2XMLGregorianCalendar(solicitudFirmaPortafirma.solicitudFechaFin));
			solicitudFirmaPropiedadesType.setSolicitudFechaInicio(DateTime2XMLGregorianCalendar(solicitudFirmaPortafirma.solicitudFechaInicio));
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solicitudFirmaPropiedadesType.setFirmaUriFuncionario(solicitudFirmaPortafirma.firmaUriFuncionario);
		solicitudFirmaPropiedadesType.setMateria(solicitudFirmaPortafirma.materia);
		//La firma se solicita en serie para todos los firmantes
		if (solicitudFirmaPortafirma.mecanismoFirma == null)
			solicitudFirmaPropiedadesType.setMecanismoFirma(MecanismoFirmaEnumType.SERIE);
		else
			solicitudFirmaPropiedadesType.setMecanismoFirma(MecanismoFirmaEnumType.fromValue(solicitudFirmaPortafirma.mecanismoFirma));
		solicitudFirmaPropiedadesType.setOrigen(solicitudFirmaPortafirma.origen);
		if (solicitudFirmaPortafirma.procedimiento == null)
			solicitudFirmaPropiedadesType.setProcedimiento(properties.FapProperties.get("fap.platino.gestordocumental.procedimiento"));
		else
			solicitudFirmaPropiedadesType.setProcedimiento(solicitudFirmaPortafirma.procedimiento);
		solicitudFirmaPropiedadesType.setSolicitudEstadoComentario(solicitudFirmaPortafirma.solicitudEstadoComentario);
		solicitudFirmaPropiedadesType.setTema(solicitudFirmaPortafirma.tema);
		// Las solicitudes de firma son de tipo SOLICITUD por lo que solicitudFirmaPortafirma.tipoDocumento debe ser "SOL"
		solicitudFirmaPropiedadesType.setTipoDocumento(platinoProcedimientosPort.getTipoDocumento(solicitudFirmaPortafirma.tipoDocumento).getTipo()); 
		try {
			solicitudFirmaPortafirma.uriFuncionarioDestinatario = platinoDBOrgPort.recuperarURIPersona(solicitudFirmaPortafirma.idDestinatario);
			solicitudFirmaPropiedadesType.setUriFuncionarioDestinatario(solicitudFirmaPortafirma.uriFuncionarioDestinatario);
			solicitudFirmaPortafirma.uriFuncionarioSolicitante = platinoDBOrgPort.recuperarURIPersona(solicitudFirmaPortafirma.idSolicitante);
			solicitudFirmaPropiedadesType.setUriFuncionarioSolicitante(solicitudFirmaPortafirma.uriFuncionarioSolicitante);
		} catch (DBOrganizacionException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solicitudFirmaPortafirma.save();
		return solicitudFirmaPropiedadesType;
	}
	
	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		
		//Configuramos el usuario del agente actual, recuperando su URI de BDOrg de platino
		setupSecurityHeadersWithUser(solicitudFirmaPortafirma.idSolicitante);
		
		try {
			// Crear solicitud de firma
			SolicitudFirmaPropiedadesType solicitudFirmaPropiedadesType = crearSolicitudFirmaPlatino(solicitudFirmaPortafirma);
			solicitudFirmaPortafirma.uriSolicitud = portafirmaPort.crearSolicitudFirma(solicitudFirmaPropiedadesType);
			
			// Adjuntamos el documento a firmar y los documentos anexos
			asociarDocumento(solicitudFirmaPortafirma.uriSolicitud, solicitudFirmaPortafirma.idSolicitante, solicitudFirmaPortafirma.documentosFirma, TipoDocumentacionEnumType.PRINCIPAL);
			asociarDocumento(solicitudFirmaPortafirma.uriSolicitud, solicitudFirmaPortafirma.idSolicitante, solicitudFirmaPortafirma.documentosConsulta, TipoDocumentacionEnumType.ANEXA); 
			
			PortafirmaCrearSolicitudResponse portafirmaCrearSolicitudResponse = new PortafirmaCrearSolicitudResponse();
			portafirmaCrearSolicitudResponse.setIdSolicitud(solicitudFirmaPortafirma.uriSolicitud);
			solicitudFirmaPortafirma.solicitudEstadoComentario = "La solicitud de firma de portafirma ha sido enviada.";
			solicitudFirmaPortafirma.save();
			portafirmaCrearSolicitudResponse.setComentarios(solicitudFirmaPortafirma.solicitudEstadoComentario);
			return portafirmaCrearSolicitudResponse;
			
		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.info("Error al crear la solicitud de firma: " + e.getMessage());
			throw new PortafirmaFapServiceException("Error al crear la solicitud de firma: " + e.getMessage(), e);
			
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	@Override
	public String obtenerEstadoFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		setupSecurityHeadersWithUser(solicitudFirmaPortafirma.idSolicitante);
		try {
			SolicitudFirmaType solicitudFirmaType = portafirmaPort.obtenerSolicitudFirma(solicitudFirmaPortafirma.uriSolicitud, true);
			solicitudFirmaPortafirma.solicitudEstado = solicitudFirmaType.getPropiedades().getSolicitudEstado().value();
			solicitudFirmaPortafirma.solicitudEstadoComentario = solicitudFirmaType.getPropiedades().getSolicitudEstadoComentario();
			solicitudFirmaPortafirma.save();
			play.Logger.info("El estado de la solicitud de firma de portafirma "+solicitudFirmaPortafirma.uriSolicitud+" es "+solicitudFirmaPortafirma.solicitudEstado);
			return solicitudFirmaPortafirma.solicitudEstado;
		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.error("Error al obtener el estado de la solicitud de firma de portafirma: " + e.getMessage(), e);
			throw new PortafirmaFapServiceException("Error al obtener el estado de la solicitud de firma de portafirma: " + e.getMessage(), e);
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	// TODO: ¿Quitar estado FIRMADA_Y_REENVIADA?
	@Override
	public boolean comprobarSiSolicitudFirmada(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		String estadoSolicitudFirma = obtenerEstadoFirma(solicitudFirmaPortafirma);
		if ((EstadoSolicitudEnumType.FIRMADA.value().equalsIgnoreCase(estadoSolicitudFirma)) || (EstadoSolicitudEnumType.FIRMADA_Y_REENVIADA.value().equalsIgnoreCase(estadoSolicitudFirma))) {
			play.Logger.info("La solicitud de firma de portafirma "+solicitudFirmaPortafirma.uriSolicitud+" ha sido firmada.");
			//play.Logger.info("La solicitud de firma de portafirma "+solicitudFirmaPortafirma.uriSolicitud+" ha sido firmada y finalizada.");
			return true;
		}
		else {
			play.Logger.info("La solicitud de firma de portafirma "+solicitudFirmaPortafirma.uriSolicitud+" no ha sido firmada y finalizada.");
			return false;
		}
	}

	@Override
	public List<ComboItem> obtenerUsuariosAdmitenEnvio() throws PortafirmaFapServiceException {
		String[] listaDestinatarios = properties.FapProperties.get("fap.platino.portafirma.destinatarios").split(",");
		if ((listaDestinatarios != null) && (listaDestinatarios.length % 2) == 0) {
			List<ComboItem> listaUsuarioAdmiteEnvio = new ArrayList<ComboItem>();
			for (int i = 0; i < listaDestinatarios.length; i++) {
				listaUsuarioAdmiteEnvio.add(new ComboItem(listaDestinatarios[i], listaDestinatarios[++i]));
			}
			play.Logger.info("La lista de usuarios que admiten envío de solicitudes de firma de portafirma ha sido creada con éxito.");
			return listaUsuarioAdmiteEnvio;
		}
		else {
			play.Logger.error("La lista de usuarios que admiten envío de solicitudes de firma de portafirma no se ha podido crear (compruebe que está bien especificada en el fichero application.conf).");
			Messages.error("La lista de usuarios que admiten envío de solicitudes de firma de portafirma no se ha podido crear.");
			throw new PortafirmaFapServiceException("La lista de usuarios que admiten envío de solicitudes de firma de portafirma no se ha podido crear (compruebe que está bien especificada en el fichero application.conf).");
		}
	}

	@Override
	public void eliminarSolicitudFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		play.Logger.warn("Actualmente este método no hace nada pues la llamada a eliminarSolicitudFirma del servicio de Platino falla pasándole los parámetros correctos.");
//		try {
//			portafirmaPort.eliminarSolicitudFirma(solicitudFirmaPortafirma.uriSolicitud, solicitudFirmaPortafirma.comentarioSolicitante);
//			obtenerEstadoFirma(solicitudFirmaPortafirma);
//		} catch (SolicitudFirmaExcepcion e) {
//			play.Logger.error("No se ha podido eliminar la solicitud "+solicitudFirmaPortafirma.uriSolicitud+": "+e);
//			throw new PortafirmaFapServiceException("No se ha podido eliminar la solicitud "+solicitudFirmaPortafirma.uriSolicitud, e);
//		}
	}
}
