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
import models.Agente;
import models.Convocatoria;
import models.Documento;
import models.ExpedientePlatino;
import models.ResolucionFAP;
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
	
	private static XMLGregorianCalendar DateTime2XMLGregorianCalendar(DateTime fecha) throws DatatypeConfigurationException {
		if (fecha == null)
			return null;
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTime(fecha.toDate());
		gcal.setTimeInMillis(fecha.getMillis());
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}
	
	private PrioridadEnumType getPrioridadEnumTypeFromValue (String strPrioridad) {
		if (strPrioridad.equalsIgnoreCase("ALTA"))
			return PrioridadEnumType.ALTA;
		if ((strPrioridad.equalsIgnoreCase("NORMAL")) || (strPrioridad.equalsIgnoreCase("MEDIA"))) // En el portafirma es normal
			return PrioridadEnumType.NORMAL;
		return PrioridadEnumType.BAJA;
	}
	
	private void setupSecurityHeadersWithUser(String uid) {
		try {
			
			String userUri = platinoDBOrgPort.recuperarURIPersona(uid);
			WSUtils.configureSecurityHeadersWithUser(portafirmaPort, userUri);
			
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al configuar cabecera de seguridad para usuario: " + uid + ". " + e.getMessage());
			//throw new PortafirmaFapServiceException("Error al configuar cabecera de seguridad para usuario: " + uid + ". " + e.getMessage(), e);
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
	
	//Método que crea un objeto SolicitudFirmaPropiedadesType a partir de una Resolución FAP
	private SolicitudFirmaPropiedadesType getCrearSolicitudWSTypeFromResolucionFAP (ResolucionFAP resolucion, String uidSolicitante, String uidDestinatario) {
		SolicitudFirmaPropiedadesType solFirma = new SolicitudFirmaPropiedadesType();
		
		try {
			String uriSolicitante = platinoDBOrgPort.recuperarURIPersona(uidSolicitante);
			String uriDestinatario= platinoDBOrgPort.recuperarURIPersona(uidDestinatario);
			solFirma.setUriFuncionarioSolicitante(uriSolicitante);
			solFirma.setUriFuncionarioDestinatario(uriDestinatario);
			
			String tema = "TEMA";
			//solFirma.setTema(resolucion.descripcion);
			solFirma.setTema(tema);
			
			String materia = "PRUEBA DESDE FAP";
			//solFirma.setMateria(resolucion.sintesis);
			solFirma.setMateria(materia);
			
			//solFirma.setProcedimiento(properties.FapProperties.get("fap.platino.gestordocumental.procedimiento"));
			//solFirma.setExpediente(properties.FapProperties.get("fap.platino.gestordocumental.expediente.descripcion"));
			
			//Las solicitudes de firma son de tipo SOLICITUD
			String tipoDoc = platinoProcedimientosPort.getTipoDocumento("SOL").getTipo();
			solFirma.setTipoDocumento(tipoDoc);
			
			//La firma se solicita en serie para todos los firmantes
			solFirma.setMecanismoFirma(MecanismoFirmaEnumType.SERIE);
			solFirma.setPlazoMaximo(DateTime2XMLGregorianCalendar((new DateTime()).plusDays(ResolucionControllerFAP.getDiasLimiteFirma(resolucion.id))));
			
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		} catch (DBOrganizacionException_Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return solFirma;
	}
	
	private void asociarDocumentoSolicitudFirma (ResolucionFAP resolucion) throws SolicitudFirmaExcepcion {
		
		//Copiamos el documento oficial a Platino si no esta
		subirDocumentoAEDaPlatino(resolucion.registro.oficial, resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
		
		//Añadimos el documento a firmar a la solicitud
		DocumentoSolicitudFirmaType docPlatino = new DocumentoSolicitudFirmaType();
		docPlatino.setUriDocumento(resolucion.registro.oficial.uriPlatino);
		System.out.println("Adjuntamos documento a firmar a la solicitud de firma");
		portafirmaPort.crearDocumento(resolucion.idSolicitudFirma, docPlatino, TipoDocumentacionEnumType.PRINCIPAL);
	}
	
	private void asociarAnexosSolicitudFirma(ResolucionFAP resolucion) throws SolicitudFirmaExcepcion {
		// Añadimos todos los documentos que se utilizan para consulta
		for (Documento doc: resolucion.docConsultaPortafirmasResolucion) {
			//Copiamos el documento oficial a Platino si no esta
			subirDocumentoAEDaPlatino(doc, resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
			
			//Añadimos el documento a firmar a la solicitud
			DocumentoSolicitudFirmaType docPlatino = new DocumentoSolicitudFirmaType();
			docPlatino.setUriDocumento(doc.uriPlatino);
			System.out.println("Adjuntamos documento a firmar a la solicitud de firma");
			portafirmaPort.crearDocumento(resolucion.idSolicitudFirma, docPlatino, TipoDocumentacionEnumType.ANEXA);
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

	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		
		//Configuramos el usuario del agente actual, recuperando su URI de BDOrg de platino
		setupSecurityHeadersWithUser(resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
		
		try {
			
			//Crear solicitud de firma a partir de resolucion
			SolicitudFirmaPropiedadesType sfpt = getCrearSolicitudWSTypeFromResolucionFAP(resolucion, resolucion.solicitudFirmaJefeServicio.usuarioLDAP, resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
			
			//Creamos la solicitud de firma (guardar la uri en el id)
			resolucion.idSolicitudFirma = portafirmaPort.crearSolicitudFirma(sfpt);
			resolucion.save();
			
			//Adjuntamos el documento a firmar y los documentos anexos
			asociarDocumentoSolicitudFirma(resolucion);
			asociarAnexosSolicitudFirma(resolucion);
			
			PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
			response.setIdSolicitud(resolucion.idSolicitudFirma);
			response.setComentarios(sfpt.getSolicitudEstadoComentario());
			return response;
			
		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.info("Error al crear la solicitud de firma: " + e.getMessage());
			throw new PortafirmaFapServiceException("Error al crear la solicitud de firma: " + e.getMessage(), e);
			
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(String titulo, String descripcion, String tipoSolicitud, String prioridad, XMLGregorianCalendar fechaTopeFirma,
																String idSolicitante, String idDestinatario, String emailNotificacion, ResolucionFAP resolucion)
																throws PortafirmaFapServiceException {
		
		//Configuramos el usuario del agente actual, recuperando su URI de BDOrg de platino
		setupSecurityHeadersWithUser(resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
		
		try {
			
			//Crear solicitud de firma a partir de resolucion
			SolicitudFirmaPropiedadesType sfpt = getCrearSolicitudWSTypeFromResolucionFAP(resolucion, idSolicitante, idDestinatario);
			sfpt.setPlazoMaximo(fechaTopeFirma);
			sfpt.setTema(titulo);
			sfpt.setMateria(descripcion);
			
			//Creamos la solicitud de firma (guardar la uri en el id)
			resolucion.idSolicitudFirma = portafirmaPort.crearSolicitudFirma(sfpt);
			resolucion.save();
			
			//Adjuntamos el documento a firmar y los documentos anexos
			asociarDocumentoSolicitudFirma(resolucion);
			asociarAnexosSolicitudFirma(resolucion);
			
			PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
			response.setIdSolicitud(resolucion.idSolicitudFirma);
			response.setComentarios(sfpt.getSolicitudEstadoComentario());
			return response;
			
		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.info("Error al crear la solicitud de firma: " + e.getMessage());
			throw new PortafirmaFapServiceException("Error al crear la solicitud de firma: " + e.getMessage(), e);
			
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	@Override
	public String obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		
		setupSecurityHeadersWithUser(resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
		
		try {
			
			SolicitudFirmaType solFirma = portafirmaPort.obtenerSolicitudFirma(resolucion.idSolicitudFirma, true);
			
			return solFirma.getPropiedades().getSolicitudEstado().value();

		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.info("Error al obtener el estado de la solicitud de firma");
			throw new PortafirmaFapServiceException("Error al obtener el estado de la solicitud de firma: " + e.getMessage(), e);
			
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	@Override
	public String obtenerEstadoFirma(ResolucionFAP resolucion, String idSolicitudFirma, String idUsuario) throws PortafirmaFapServiceException {
		
		setupSecurityHeadersWithUser(resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
		
		try {
			SolicitudFirmaType solFirma = portafirmaPort.obtenerSolicitudFirma(idSolicitudFirma, true);
			return solFirma.getPropiedades().getSolicitudEstado().value();
			
		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.info("Error al obtener el estado de la solicitud de firma: " + e.getMessage(), e);
			throw new PortafirmaFapServiceException("Error al obtener el estado de la solicitud de firma: " + e.getMessage(), e);
			
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	@Override
	public boolean comprobarSiResolucionFirmada(ResolucionFAP resolucion, String idSolicitudFirma) throws PortafirmaFapServiceException {
		
		setupSecurityHeadersWithUser(resolucion.solicitudFirmaJefeServicio.usuarioLDAP);
		
		try {
			SolicitudFirmaType solFirma = portafirmaPort.obtenerSolicitudFirma(idSolicitudFirma, true);
			return ((EstadoSolicitudEnumType.FIRMADA.equals(solFirma.getPropiedades().getSolicitudEstado()))
				|| (EstadoSolicitudEnumType.FIRMADA_Y_REENVIADA.equals(solFirma.getPropiedades().getSolicitudEstado())));
			
		} catch (SolicitudFirmaExcepcion e) {
			play.Logger.info("Error al comprobar si la solicitud de firma se ha firmado: " + e.getMessage(), e);
			throw new PortafirmaFapServiceException("Error al comprobar si la solicitud de firma se ha firmado: " + e.getMessage(), e);
			
		} finally {
			//Restauramos el backoffice en las cabeceras de seguridad
			restoreSecurityHeadersBackoffice();
		}
	}

	@Override
	public List<ComboItem> obtenerUsuariosAdmitenEnvio() throws PortafirmaFapServiceException {
		
		List<ComboItem> ret = new ArrayList<ComboItem>();
		try {
			ret.add(new ComboItem(platinoDBOrgPort.recuperarURIPersona("dgonmor"), "dgonmor"));
			
		} catch (DBOrganizacionException_Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	@Override
	public void eliminarSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		resolucion.solicitudFirmaJefeServicio.passwordLDAP = null;
		resolucion.solicitudFirmaJefeServicio.usuarioLDAP = null;
		resolucion.solicitudFirmaJefeServicio.save();
	}
}
