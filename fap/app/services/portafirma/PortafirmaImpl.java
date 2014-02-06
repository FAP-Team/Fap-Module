package services.portafirma;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.joda.time.DateTime;

import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;

import models.Agente;
import models.Documento;
import models.LineaResolucionFAP;
import models.Registro;
import models.ResolucionFAP;

import enumerado.fap.gen.EstadoPortafirmaEnum;
import es.gobcan.aciisi.portafirma.ws.PortafirmaException;
import es.gobcan.aciisi.portafirma.ws.PortafirmaService;
import es.gobcan.aciisi.portafirma.ws.PortafirmaSoapService;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.DocumentoAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.DocumentoType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoDocumentoEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.UsuarioType;

import platino.PlatinoProxy;
import properties.FapProperties;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;

public class PortafirmaImpl implements PortafirmaFapService {
	
	private static PortafirmaService portafirmaService;
	
	static {
		URL wsdlURL =PortafirmaFapService.class.getClassLoader()
				.getResource("wsdl/portafirma.wsdl");

		portafirmaService= new PortafirmaSoapService(wsdlURL).getPortafirmaSoapService();
		BindingProvider bp = (BindingProvider) portafirmaService;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				FapProperties.get("portafirma.webservice.wsdlURL"));
		PlatinoProxy.setProxy(portafirmaService);
	}

	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		CrearSolicitudType solFirma = getCrearSolicitudWSTypeFromResolucionFAP(resolucion);
		
		CrearSolicitudResponseType wsType = new CrearSolicitudResponseType();
		try {
			wsType = portafirmaService.crearSolicitud(solFirma);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
		PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
		response.setIdSolicitud(wsType.getIdSolicitud());
		response.setComentarios(wsType.getComentario());
		return response;
	}
	
	private CrearSolicitudType getCrearSolicitudWSTypeFromResolucionFAP (ResolucionFAP resolucion) {
		CrearSolicitudType solFirma=new CrearSolicitudType();
		solFirma.setTitulo(resolucion.tituloInterno);
        if ((resolucion.descripcion == null) || (resolucion.descripcion.trim().equals("")))
              solFirma.setDescripcion(resolucion.sintesis);
        else
              solFirma.setDescripcion(resolucion.descripcion);
		Agente agenteActual = AgenteController.getAgente();
		// Agente activo
		solFirma.setIdSolicitante(FapProperties.get("portafirma.usuario"));
		// Destinatario -> Jefe de Servicio
		solFirma.setIdDestinatario(resolucion.jefeDeServicio);
		solFirma.setComentario(""); // Dejarlo vacío, antes: resolucion.observaciones
		// Email del agente activo
		solFirma.setEmailNotificacion(agenteActual.email);
		solFirma.setTipoSolicitud(TipoSolicitudEnumType.RESOLUCION);		
		solFirma.setPrioridad(getEnumTypeFromValue(resolucion.prioridadFirma));
		
		// Documentos a Firmar
		Integer numOrden = new Integer(1);
		ListaDocumentosAedType listaDocumentos = new ListaDocumentosAedType();
		DocumentoAedType docAedType = new DocumentoAedType();
		docAedType.setUriAed(resolucion.registro.oficial.uri);
		docAedType.setTipoDocumento(TipoDocumentoEnumType.FIRMA);
		docAedType.setNumeroOrden(numOrden.toString());
		docAedType.setDescripcion(resolucion.registro.oficial.descripcionVisible);
		listaDocumentos.getListaDocumento().add(docAedType);
		
		// Añadimos todos los documentos que se utilizan para consulta
		for (Documento doc: resolucion.docConsultaPortafirmasResolucion) {
			numOrden++;
			DocumentoAedType docPortafirma = new DocumentoAedType();
			docPortafirma.setTipoDocumento(TipoDocumentoEnumType.CONSULTA);
			docPortafirma.setUriAed(doc.uri);
			docPortafirma.setDescripcion(doc.descripcionVisible);
			docPortafirma.setNumeroOrden(numOrden.toString());
			listaDocumentos.getListaDocumento().add(docPortafirma);
		}
		solFirma.setDocumentosAed(listaDocumentos);
		
		try {
			//solFirma.setFechaTopeFirma(DateTime2XMLGregorianCalendar((new DateTime()).plusDays(ResolucionControllerFAP.getDiasLimiteFirma(resolucion.id))));
			solFirma.setFechaTopeFirma(DateTime2XMLGregorianCalendar(resolucion.fechaTopeFirma)); // TODO Comprobar fecha en el portafirmas
		} catch (DatatypeConfigurationException e) {
			play.Logger.error("Error al setear la fecha tope de firma."+ e);
		}
		
		return solFirma;
	}
	
	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (String titulo, String descripcion, String tipoSolicitud, String prioridad, XMLGregorianCalendar fechaTopeFirma, String idSolicitante, String idDestinatario, String emailNotificacion, ResolucionFAP resolucion) throws PortafirmaFapServiceException {

		CrearSolicitudType solFirma = new CrearSolicitudType();
		solFirma.setTitulo(titulo);
		solFirma.setDescripcion(descripcion);
		solFirma.setTipoSolicitud(TipoSolicitudEnumType.fromValue(tipoSolicitud));	
		solFirma.setPrioridad(PrioridadEnumType.fromValue(prioridad));
		solFirma.setFechaTopeFirma(fechaTopeFirma);
		solFirma.setIdSolicitante(idSolicitante);
		solFirma.setIdDestinatario(idDestinatario);
		solFirma.setEmailNotificacion(emailNotificacion);
		
		ListaDocumentosAedType documentosAed = new ListaDocumentosAedType();
		Integer numOrden = new Integer(1);
		for (LineaResolucionFAP linea: resolucion.lineasResolucion) {
			if (!linea.registro.fasesRegistro.firmada) {
				DocumentoAedType docAedType = new DocumentoAedType();
				docAedType.setUriAed(linea.registro.oficial.uri);
				docAedType.setTipoDocumento(TipoDocumentoEnumType.FIRMA);
				docAedType.setNumeroOrden(numOrden.toString());
				docAedType.setDescripcion(linea.registro.oficial.descripcionVisible);
				documentosAed.getListaDocumento().add(docAedType);
				numOrden++;
			}
		}
		
		solFirma.setDocumentosAed(documentosAed);
		
		CrearSolicitudResponseType wsType = new CrearSolicitudResponseType();
		try {
			wsType = portafirmaService.crearSolicitud(solFirma);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
		PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
		response.setIdSolicitud(wsType.getIdSolicitud());
		response.setComentarios(wsType.getComentario());
		return response;
	}
	
	private static XMLGregorianCalendar DateTime2XMLGregorianCalendar(DateTime fecha) throws DatatypeConfigurationException {
		if (fecha == null)
			return null;
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTime(fecha.toDate());
		gcal.setTimeInMillis(fecha.getMillis());
		gcal.set(fecha.getYear(), fecha.getMonthOfYear(), fecha.getDayOfMonth(), 23, 59, 59);
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}
	
	private PrioridadEnumType getEnumTypeFromValue (String strPrioridad) {
		if (strPrioridad.equalsIgnoreCase("ALTA"))
			return PrioridadEnumType.ALTA;
		if ((strPrioridad.equalsIgnoreCase("NORMAL")) || (strPrioridad.equalsIgnoreCase("MEDIA"))) // En el portafirma es normal
			return PrioridadEnumType.NORMAL;
		return PrioridadEnumType.BAJA;
	}
	
	public boolean isConfigured() {
		try {
			return (portafirmaService.obtenerVersion() != null);
		} catch (PortafirmaException e) {
			play.Logger.error("Error al obtener la versión del servicio de Portafirma" + e);
		} catch (Exception e1) {
			play.Logger.error("Error al obtener la versión del servicio de Portafirma" + e1);
		}
		return false;
	}
	
	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Portafirma ha sido inyectado con PortafirmaService y está operativo.");
		else
			play.Logger.info("El servicio de Portafirma ha sido inyectado con PortafirmaService y NO está operativo.");
	}

	@Override
	public String obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		try {
			ObtenerEstadoSolicitudType oEstado = new ObtenerEstadoSolicitudType();
			oEstado.setIdSolicitud(resolucion.idSolicitudFirma);
			oEstado.setIdUsuario(FapProperties.get("portafirma.usuario"));
			return portafirmaService.obtenerEstadoSolicitud(oEstado).getEstado();
		} catch (Exception e) {
			throw new PortafirmaFapServiceException("Error al comprobar el estado de la solicitud en el portafirma", e);
		}
	}

	@Override
	public String obtenerEstadoFirma(ResolucionFAP resolucion, String idSolicitudFirma, String idUsuario) throws PortafirmaFapServiceException {
		try {
			ObtenerEstadoSolicitudType oEstado = new ObtenerEstadoSolicitudType();
			oEstado.setIdSolicitud(idSolicitudFirma);
			oEstado.setIdUsuario(idUsuario);
			return portafirmaService.obtenerEstadoSolicitud(oEstado).getEstado();
		} catch (Exception e) {
			throw new PortafirmaFapServiceException("Error al comprobar el estado de la solicitud en el portafirma", e);
		}
	}
	
	@Override
	public void eliminarSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String obtenerVersion() throws PortafirmaFapServiceException {
		try {
			return portafirmaService.obtenerVersion();
		} catch (PortafirmaException e) {
			play.Logger.error("No se ha podido obtener la versión del portafirma: "+e);
			throw new PortafirmaFapServiceException("No se ha podido obtener la versión del portafirma", e);
		}
	}

	@Override
	public boolean comprobarSiResolucionFirmada(ResolucionFAP resolucion, String idSolicitudFirma) throws PortafirmaFapServiceException {
		try {
			return portafirmaService.comprobarSolicitudFinalizada(idSolicitudFirma, FapProperties.get("portafirma.usuario"));
		} catch (PortafirmaException e) {
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
	}

	@Override
	public List<ComboItem> obtenerUsuariosAdmitenEnvio() 
			throws PortafirmaFapServiceException {
		List<UsuarioType> listaUsuarios = null;
		List<ComboItem> listResult = new ArrayList<ComboItem>();
		try {
			listaUsuarios = portafirmaService.obtenerUsuariosAdmitenEnvio(FapProperties.get("portafirma.usuario"));
			for (UsuarioType user: listaUsuarios) {
				listResult.add(new ComboItem(user.getIdUsuario(), user.getIdUsuario()+ " - "+user.getNombreCompleto()));
			}
		} catch (PortafirmaException e) {
			throw new PortafirmaFapServiceException("Error al obtener los usuarios que admiten envíos del portafirma", e);
		}
		return listResult;
	}


}
