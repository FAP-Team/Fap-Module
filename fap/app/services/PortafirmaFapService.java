package services;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import models.Registro;
import models.ResolucionFAP;

public interface PortafirmaFapService {

	public void mostrarInfoInyeccion();
	public String obtenerVersion () throws PortafirmaFapServiceException; 
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (String titulo, String descripcion, String tipoSolicitud, String prioridad, XMLGregorianCalendar fechaTopeFirma, String idSolicitante, String idDestinatario, String emailNotificacion, ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public String obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public String obtenerEstadoFirma(String idSolicitudFirma, String idUsuario) throws PortafirmaFapServiceException;
	public void eliminarSolicitudFirma () throws PortafirmaFapServiceException;
	public boolean comprobarSiResolucionFirmada (String idSolicitudFirma) throws PortafirmaFapServiceException;
	public List<ComboItem> obtenerUsuariosAdmitenEnvio () throws PortafirmaFapServiceException;
}
