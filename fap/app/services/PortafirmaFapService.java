package services;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;

import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import models.ResolucionFAP;

public interface PortafirmaFapService {

	public void mostrarInfoInyeccion();
	public String obtenerVersion () throws PortafirmaFapServiceException; 
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (String titulo, String descripcion, TipoSolicitudEnumType tipoSolicitud, PrioridadEnumType prioridad, XMLGregorianCalendar fechaTopeFirma, String idSolicitante, String idDestinatario, String comentario, String emailNotificacion, String urlRedireccion, String urlNotificacion, String flujoSolicitud, ListaDocumentosAedType documentosAed, ListaDocumentosType documentos) throws PortafirmaFapServiceException;
	public ObtenerEstadoSolicitudResponseType obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public ObtenerEstadoSolicitudResponseType obtenerEstadoFirma(String idSolicitudFirma, String idUsuario) throws PortafirmaFapServiceException;
	public void eliminarSolicitudFirma () throws PortafirmaFapServiceException;
	public boolean comprobarSiResolucionFirmada (String idSolicitudFirma) throws PortafirmaFapServiceException;
	public List<ComboItem> obtenerUsuariosAdmitenEnvio () throws PortafirmaFapServiceException;
}
