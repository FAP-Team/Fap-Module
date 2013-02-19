package services.portafirma;

import java.net.URL;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.joda.time.DateTime;

import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;

import models.Agente;
import models.ResolucionFAP;

import enumerado.fap.gen.EstadoPortafirmaEnum;
import es.gobcan.aciisi.portafirma.ws.PortafirmaException;
import es.gobcan.aciisi.portafirma.ws.PortafirmaService;
import es.gobcan.aciisi.portafirma.ws.PortafirmaSoapService;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;

import platino.PlatinoProxy;
import properties.FapProperties;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;

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
		} catch (PortafirmaException e) {
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
		solFirma.setDescripcion(resolucion.descripcion);
		Agente agenteActual = AgenteController.getAgente();
		// Agente activo
		solFirma.setIdSolicitante(agenteActual.username);
		// Destinatario -> Jefe de Servicio
		solFirma.setIdDestinatario(resolucion.jefeDeServicio);
		solFirma.setComentario(resolucion.observaciones);
		// Email del agente activo
		solFirma.setEmailNotificacion(agenteActual.email);
		solFirma.setTipoSolicitud(TipoSolicitudEnumType.RESOLUCION);		
		solFirma.setPrioridad(getEnumTypeFromValue(resolucion.prioridadFirma));
		try {
			solFirma.setFechaTopeFirma(DateTime2XMLGregorianCalendar((new DateTime()).plusDays(ResolucionControllerFAP.getDiasLimiteFirma(resolucion.id))));
		} catch (DatatypeConfigurationException e) {
			play.Logger.error("Error al setear la fecha tope de firma.", e);
		}
		
		return solFirma;
	}
	
	private static XMLGregorianCalendar DateTime2XMLGregorianCalendar(DateTime fecha) throws DatatypeConfigurationException {
		if (fecha == null)
			return null;
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTime(fecha.toDate());
		gcal.setTimeInMillis(fecha.getMillis());
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}
	
	private PrioridadEnumType getEnumTypeFromValue (String strPrioridad) {
		if (strPrioridad.equalsIgnoreCase("ALTA"))
			return PrioridadEnumType.ALTA;
		if (strPrioridad.equalsIgnoreCase("NORMAL"))
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
	public void obtenerEstadoFirma() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void eliminarSolicitudFirma() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String obtenerVersion() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean comprobarSiResolucionFirmada(String idSolicitudFirma, String idAgente) throws PortafirmaFapServiceException {
		try {
			return portafirmaService.comprobarSolicitudFinalizada(idSolicitudFirma, idAgente);
		} catch (PortafirmaException e) {
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
	}

}
