package services.portafirma;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.joda.time.DateTime;

import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import models.Agente;
import models.Documento;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudFirmaPortafirma;
import enumerado.fap.gen.EstadoPortafirmaEnum;
import es.gobcan.aciisi.portafirma.ws.PortafirmaException;
import es.gobcan.aciisi.portafirma.ws.PortafirmaService;
import es.gobcan.aciisi.portafirma.ws.PortafirmaSoapService;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.DocumentoAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.DocumentoType;
import es.gobcan.aciisi.portafirma.ws.dominio.EliminarSolicitudType;
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
		CrearSolicitudType solFirma = new CrearSolicitudType();
		solFirma.setTitulo(resolucion.tituloInterno);
        if ((resolucion.descripcion == null) || (resolucion.descripcion.trim().equals("")))
              solFirma.setDescripcion(resolucion.sintesis);
        else
              solFirma.setDescripcion(resolucion.descripcion);
		Agente agenteActual = AgenteController.getAgente();
		// Agente activo
		solFirma.setIdSolicitante(resolucion.solicitudFirmaPortafirma.idSolicitante);
		// Destinatario -> Jefe de Servicio
		solFirma.setIdDestinatario(resolucion.solicitudFirmaPortafirma.idDestinatario);
		solFirma.setComentario(""); // Dejarlo vacío, antes: resolucion.observaciones
		// Email del agente activo
		solFirma.setEmailNotificacion(agenteActual.email);
		solFirma.setTipoSolicitud(TipoSolicitudEnumType.RESOLUCION);
		solFirma.setPrioridad(getEnumTypeFromValue(resolucion.solicitudFirmaPortafirma.prioridad));
		
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
			solFirma.setFechaTopeFirma(DateTime2XMLGregorianCalendar(resolucion.solicitudFirmaPortafirma.plazoMaximo));
		} catch (DatatypeConfigurationException e) {
			play.Logger.error("Error al setear la fecha tope de firma."+ e);
		}
		
		return solFirma;
	}
	
	private void documentos2DocumentossAedType (CrearSolicitudType solFirma, SolicitudFirmaPortafirma solicitudFirmaPortafirma) {
		
		if (((solicitudFirmaPortafirma.documentosFirma != null) &&  (!solicitudFirmaPortafirma.documentosFirma.isEmpty()))
				|| (((solicitudFirmaPortafirma.documentosConsulta != null) &&  (!solicitudFirmaPortafirma.documentosConsulta.isEmpty())))) {
			
			Integer numeroOrden = new Integer(1);
			ListaDocumentosAedType listaDocumentosAedType = new ListaDocumentosAedType();
			
			// Documentos de firma
			for (Documento documento: solicitudFirmaPortafirma.documentosFirma) {
				DocumentoAedType documentoAedType = new DocumentoAedType();
				documentoAedType.setTipoDocumento(TipoDocumentoEnumType.FIRMA);
				documentoAedType.setUriAed(documento.uri);
				documentoAedType.setDescripcion(documento.descripcionVisible);
				documentoAedType.setNumeroOrden(numeroOrden.toString());
				listaDocumentosAedType.getListaDocumento().add(documentoAedType);
				numeroOrden++;
			}
			
			// Documentos de consulta (añadidos)
			for (Documento documento: solicitudFirmaPortafirma.documentosConsulta) {
				DocumentoAedType documentoAedType = new DocumentoAedType();
				documentoAedType.setTipoDocumento(TipoDocumentoEnumType.CONSULTA);
				documentoAedType.setUriAed(documento.uri);
				documentoAedType.setDescripcion(documento.descripcionVisible);
				documentoAedType.setNumeroOrden(numeroOrden.toString());
				listaDocumentosAedType.getListaDocumento().add(documentoAedType);
				numeroOrden++;
			}
			
			solFirma.setDocumentosAed(listaDocumentosAedType);
		}
	}
	
	// TODO: SIN TERMINAR
	private void documentos2DocumentosType (CrearSolicitudType solFirma, SolicitudFirmaPortafirma solicitudFirmaPortafirma) {
		if ((solicitudFirmaPortafirma.documento != null) && (solicitudFirmaPortafirma.documento.uri != null)) {
			Integer numeroOrden = new Integer(1);
			ListaDocumentosType listaDocumentosType = new ListaDocumentosType();
			DocumentoType documentoType = new DocumentoType();
			documentoType.setTipoDocumento(TipoDocumentoEnumType.CONSULTA);
			documentoType.setContenido(null);		//  \
			documentoType.setMimeType(null);		//   |---- TODO: Falta rellenar estos campos
			documentoType.setNombreFichero(null);	//  /
			documentoType.setDescripcion(solicitudFirmaPortafirma.documento.descripcionVisible);
			documentoType.setNumeroOrden(numeroOrden.toString());
			listaDocumentosType.getDocumento().add(documentoType);
			solFirma.setDocumentos(listaDocumentosType);
		}
	}
	
	private CrearSolicitudType crearSolicitudFirmaACIISI (SolicitudFirmaPortafirma solicitudFirmaPortafirma) {
		CrearSolicitudType solFirma = new CrearSolicitudType();
		solFirma.setTitulo(solicitudFirmaPortafirma.tema);
		solFirma.setDescripcion(solicitudFirmaPortafirma.materia);
		solFirma.setTipoSolicitud(TipoSolicitudEnumType.fromValue(solicitudFirmaPortafirma.tipoSolicitud));
		solFirma.setPrioridad(getEnumTypeFromValue(solicitudFirmaPortafirma.prioridad));
		try {
			solFirma.setFechaTopeFirma(DateTime2XMLGregorianCalendar(solicitudFirmaPortafirma.plazoMaximo));
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solFirma.setIdSolicitante(solicitudFirmaPortafirma.idSolicitante);
		solFirma.setIdDestinatario(solicitudFirmaPortafirma.idDestinatario);
		solFirma.setComentario(solicitudFirmaPortafirma.solicitudEstadoComentario);
		solFirma.setEmailNotificacion(solicitudFirmaPortafirma.emailNotificacion);
		solFirma.setUrlRedireccion(solicitudFirmaPortafirma.urlRedireccion);
		solFirma.setUrlNotificacion(solicitudFirmaPortafirma.urlNotificacion);
		solFirma.setFlujoSolicitud(solicitudFirmaPortafirma.flujoSolicitud);
		documentos2DocumentossAedType(solFirma, solicitudFirmaPortafirma);
		documentos2DocumentosType(solFirma, solicitudFirmaPortafirma);
		return solFirma;
	}
	
	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {

		CrearSolicitudType crearSolicitudType = crearSolicitudFirmaACIISI(solicitudFirmaPortafirma);
		CrearSolicitudResponseType crearSolicitudResponseType = new CrearSolicitudResponseType();
		try {
			crearSolicitudResponseType = portafirmaService.crearSolicitud(crearSolicitudType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
		PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
		response.setIdSolicitud(crearSolicitudResponseType.getIdSolicitud());
		response.setComentarios(crearSolicitudResponseType.getComentario());
		return response;
	}
	
    @Override
    public boolean entregarSolicitudFirma(String idSolicitante, String idSolicitud, String comentario) {
        return true;
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
	public String obtenerEstadoFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		try {
			ObtenerEstadoSolicitudType obtenerEstadoSolicitudType = new ObtenerEstadoSolicitudType();
			obtenerEstadoSolicitudType.setIdSolicitud(solicitudFirmaPortafirma.uriSolicitud);
			obtenerEstadoSolicitudType.setIdUsuario(solicitudFirmaPortafirma.idSolicitante);
			ObtenerEstadoSolicitudResponseType obtenerEstadoSolicitudResponseType = portafirmaService.obtenerEstadoSolicitud(obtenerEstadoSolicitudType);
			solicitudFirmaPortafirma.solicitudEstado = obtenerEstadoSolicitudResponseType.getEstado();
			solicitudFirmaPortafirma.solicitudEstadoComentario = obtenerEstadoSolicitudResponseType.getComentario();
			solicitudFirmaPortafirma.save();
			play.Logger.info("El estado de la solicitud de firma de portafirma "+solicitudFirmaPortafirma.uriSolicitud+" es "+solicitudFirmaPortafirma.solicitudEstado);
			return solicitudFirmaPortafirma.solicitudEstado;
		} catch (Exception e) {
			play.Logger.error("Error al obtener el estado de la solicitud de firma de portafirma: " + e.getMessage(), e);
			throw new PortafirmaFapServiceException("Error al obtener el estado de la solicitud de firma de portafirma: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void eliminarSolicitudFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		EliminarSolicitudType eliminarSolicitudType = new EliminarSolicitudType();
		eliminarSolicitudType.setIdSolicitud(solicitudFirmaPortafirma.uriSolicitud);
		eliminarSolicitudType.setIdSolicitante(solicitudFirmaPortafirma.idSolicitante);
		try {
			portafirmaService.eliminarSolicitud(eliminarSolicitudType);
			solicitudFirmaPortafirma.comentarioSolicitante = "La solicitud de firma fue eliminada.";
			play.Logger.info("La solicitud "+solicitudFirmaPortafirma.uriSolicitud+" ha sido eliminada correctamente.");
		} catch (PortafirmaException e) {
			play.Logger.error("No se ha podido eliminar la solicitud "+solicitudFirmaPortafirma.uriSolicitud+": "+e);
			throw new PortafirmaFapServiceException("No se ha podido eliminar la solicitud "+solicitudFirmaPortafirma.uriSolicitud, e);
		}
		catch (SOAPFaultException e) {
			play.Logger.error("La solicitud "+solicitudFirmaPortafirma.uriSolicitud+" no existe: "+e);
			throw new PortafirmaFapServiceException("La solicitud "+solicitudFirmaPortafirma.uriSolicitud+" no existe.", e);
		}
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
	public boolean comprobarSiSolicitudFirmada(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		Boolean firmada = false;
		try {
			firmada = portafirmaService.comprobarSolicitudFinalizada(solicitudFirmaPortafirma.uriSolicitud, solicitudFirmaPortafirma.idSolicitante);
			if (firmada) {
				obtenerEstadoFirma(solicitudFirmaPortafirma);
				play.Logger.info("La solicitud de firma de portafirma "+solicitudFirmaPortafirma.uriSolicitud+" ha sido firmada y finalizada.");
			}
			else
				play.Logger.info("La solicitud de firma de portafirma "+solicitudFirmaPortafirma.uriSolicitud+" no ha sido firmada y finalizada.");
			return firmada;
		} catch (PortafirmaException e) {
			play.Logger.error("Error al comprobar si la solicitud de firma de portafirma ha sido firmada y finalizada: " + e.getMessage(), e);
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
	}

	// Obtiene los jefes de servicio de la aplicación
	@Override
	public List<ComboItem> obtenerUsuariosAdmitenEnvio() throws PortafirmaFapServiceException {
		List<UsuarioType> listaUsuarios = null;
		List<ComboItem> listaResultados = new ArrayList<ComboItem>();
		try {
			// El parámetro debería ser solicitudFirmaPortafirma.idSolicitante en lugar de FapProperties.get("portafirma.usuario")
			listaUsuarios = portafirmaService.obtenerUsuariosAdmitenEnvio(FapProperties.get("portafirma.usuario"));
			for (UsuarioType usuarioType: listaUsuarios) {
				listaResultados.add(new ComboItem(usuarioType.getIdUsuario(), usuarioType.getIdUsuario()+ " - "+usuarioType.getNombreCompleto()));
			}
		} catch (PortafirmaException e) {
			play.Logger.error("Error al obtener los usuarios que admiten envíos del portafirma: " + e.getMessage(), e);
			throw new PortafirmaFapServiceException("Error al obtener los usuarios que admiten envíos del portafirma", e);
		}
		return listaResultados;
	}
	
}
