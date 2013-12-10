package services.platino;

import java.net.URL;
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
import org.apache.log4j.Logger;
import org.apache.log4j.pattern.PropertiesPatternConverter;
import org.joda.time.DateTime;

import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import models.Agente;
import models.Documento;
import models.ResolucionFAP;
import services.GestorDocumentalService;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import utils.WSUtils;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.DocumentoAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoDocumentoEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;
import es.gobcan.platino.servicios.portafirmas.dominio.*;
import es.gobcan.platino.servicios.portafirmas.servicio.solicitudfirma.*;
import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.*;

@InjectSupport
public class PlatinoPortafirmaServiceImpl implements PortafirmaFapService {
	
	private SolicitudFirmaInterface portafirmaPort;
	private PropertyPlaceholder propertyPlaceholder;
	private static Logger log = Logger.getLogger(PlatinoPortafirmaServiceImpl.class);
	
	private GestorDocumentalService gestorDocumentalPort = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
	private PlatinoProcedimientosServiceImpl procedimientosPort = InjectorConfig.getInjector().getInstance(PlatinoProcedimientosServiceImpl.class);

	@Inject
	public PlatinoPortafirmaServiceImpl(PropertyPlaceholder propertyPlaceholder) {
		this.propertyPlaceholder = propertyPlaceholder;
		
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
	
	public boolean isConfigured(){
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
	
	//Método que crea un objeto SolicitudFirmaPropiedadesType a partir de una Resolución FAP
	private SolicitudFirmaPropiedadesType getCrearSolicitudWSTypeFromResolucionFAP (ResolucionFAP resolucion) {
		SolicitudFirmaPropiedadesType solFirma = new SolicitudFirmaPropiedadesType();
		
		String uriSolicitud = "";
		solFirma.setUriSolicitud(uriSolicitud);
		
		String uriSoliciante ="platino://gobcan.es/servicios/organizacion/funcionario/360540_USER1_27011993";
		solFirma.setUriFuncionarioSolicitante(uriSoliciante);
		
		String uriDestinatario ="platino://gobcan.es/servicios/organizacion/funcionario/360540_USER2_27011993";
		solFirma.setUriFuncionarioDestinatario(uriDestinatario);
		
		String tema = "TEMA";
		//solFirma.setTema(resolucion.descripcion);
		solFirma.setTema(tema);
		
		String materia = "MATERIA";
		//solFirma.setMateria(resolucion.sintesis);
		solFirma.setMateria(materia);
		
		solFirma.setProcedimiento(properties.FapProperties.get("fap.platino.gestordocumental.procedimiento"));
		solFirma.setExpediente(properties.FapProperties.get("fap.platino.gestordocumental.expediente.descripcion"));
		
		//Las solicitudes de firma son de tipo SOLICITUD
		String tipoDoc = procedimientosPort.getTipoDocumento("SOL").getTipo();
		solFirma.setTipoDocumento(tipoDoc);
		
		//La firma se solicita en serie para todos los firmantes
		solFirma.setMecanismoFirma(MecanismoFirmaEnumType.SERIE);
		
//		try {
//			solFirma.setPlazoMaximo(DateTime2XMLGregorianCalendar((new DateTime()).plusDays(ResolucionControllerFAP.getDiasLimiteFirma(resolucion.id))));
//		} catch (DatatypeConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// Documentos a Firmar
//		Integer numOrden = new Integer(1);
//		ListaDocumentosAedType listaDocumentos = new ListaDocumentosAedType();
//		DocumentoAedType docAedType = new DocumentoAedType();
//		docAedType.setUriAed(resolucion.registro.oficial.uri);
//		docAedType.setTipoDocumento(TipoDocumentoEnumType.FIRMA);
//		docAedType.setNumeroOrden(numOrden.toString());
//		docAedType.setDescripcion(resolucion.registro.oficial.descripcionVisible);
//		listaDocumentos.getListaDocumento().add(docAedType);
//		
//		// Añadimos todos los documentos que se utilizan para consulta
//		for (Documento doc: resolucion.docConsultaPortafirmasResolucion) {
//			numOrden++;
//			DocumentoAedType docPortafirma = new DocumentoAedType();
//			docPortafirma.setTipoDocumento(TipoDocumentoEnumType.CONSULTA);
//			docPortafirma.setUriAed(doc.uri);
//			docPortafirma.setDescripcion(doc.descripcionVisible);
//			docPortafirma.setNumeroOrden(numOrden.toString());
//			listaDocumentos.getListaDocumento().add(docPortafirma);
//		}
		//solFirma.setDocumentosAed(listaDocumentos);
		
		return solFirma;
	}
	
	//END------TOOLS----------------------------
	
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
		try {
			
			//Crear solicitud de firma a partir de resolucion
			SolicitudFirmaPropiedadesType sfpt = getCrearSolicitudWSTypeFromResolucionFAP(resolucion);
			
			//Subir solicitud de firma (guardar la uri en el id)
			resolucion.idSolicitudFirma = portafirmaPort.crearSolicitudFirma(sfpt);
			
			//Subir documentos a firmar a gestor documental (si no está subido) y adjuntar
			DocumentoSolicitudFirmaType docPlatino = new DocumentoSolicitudFirmaType();
			docPlatino.setUriDocumentoSolicitudFirma(resolucion.idSolicitudFirma);
			docPlatino.setUriDocumento(resolucion.registro.oficial.uri);
			portafirmaPort.crearDocumento(resolucion.idSolicitudFirma, docPlatino, TipoDocumentacionEnumType.PRINCIPAL);
			
			//Subir documentos anexos (de consulta) a gestor documental y adjuntar
			//TipoDocumentacionEnumType.ANEXA
			
			//Firmar solicitud de firma
			
			//response.setIdSolicitud(resolucion.idSolicitudFirma);
			//response.setComentarios(sfpt.getSolicitudEstadoComentario());
			System.out.println(resolucion.idSolicitudFirma);
			
			PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
			return response;
			
		} catch (SolicitudFirmaExcepcion e) {
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
	}

	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(String titulo, String descripcion, String tipoSolicitud, String prioridad, XMLGregorianCalendar fechaTopeFirma,
																String idSolicitante, String idDestinatario, String emailNotificacion, ResolucionFAP resolucion)
																throws PortafirmaFapServiceException {
		
		//Crear solicitud de firma a partir de resolucion
		SolicitudFirmaPropiedadesType sfpt = getCrearSolicitudWSTypeFromResolucionFAP(resolucion);
		
		
		return null;
	}

	@Override
	public String obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		try {
			
			SolicitudFirmaType solFirma = portafirmaPort.obtenerSolicitudFirma(resolucion.idSolicitudFirma, true);
			
			return solFirma.getPropiedades().getSolicitudEstado().value();
			
		} catch (SolicitudFirmaExcepcion e) {
			e.printStackTrace();
			throw new PortafirmaFapServiceException("Error al obtener el estado de la solicitud de firma");
		}
	}

	@Override
	public String obtenerEstadoFirma(String idSolicitudFirma, String idUsuario) throws PortafirmaFapServiceException {
		try {
			SolicitudFirmaType solFirma = portafirmaPort.obtenerSolicitudFirma(idSolicitudFirma, true);
			
			return solFirma.getPropiedades().getSolicitudEstado().value();
			
		} catch (SolicitudFirmaExcepcion e) {
			e.printStackTrace();
			throw new PortafirmaFapServiceException("Error al obtener el estado de la solicitud de firma");
		}
	}

	@Override
	public void eliminarSolicitudFirma() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub
		//portafirmaPort.eliminarSolicitudFirma(uriSolicitud, comentario);
	}

	@Override
	public boolean comprobarSiResolucionFirmada(String idSolicitudFirma) throws PortafirmaFapServiceException {
		try {
			SolicitudFirmaType solFirma = portafirmaPort.obtenerSolicitudFirma(idSolicitudFirma, true);
			
			return (solFirma.getPropiedades().getSolicitudEstado().equals(EstadoSolicitudEnumType.FIRMADA));
			
		} catch (SolicitudFirmaExcepcion e) {
			e.printStackTrace();
			throw new PortafirmaFapServiceException("Error al comprobar si la solicitud de firma ha sido firmada");
		}
	}

	@Override
	public List<ComboItem> obtenerUsuariosAdmitenEnvio() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub
		portafirmaPort.
		return null;
	}

}
