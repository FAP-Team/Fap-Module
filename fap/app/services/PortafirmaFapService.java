package services;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import models.ResolucionFAP;
import models.SolicitudFirmaPortafirma;

public interface PortafirmaFapService {

	public void mostrarInfoInyeccion();
	public String obtenerVersion () throws PortafirmaFapServiceException; 
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException;
    public boolean entregarSolicitudFirma(String idSolicitud, String comentario);
	public String obtenerEstadoFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException;
	public void eliminarSolicitudFirma (SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException;
	public boolean comprobarSiSolicitudFirmada (SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException;
	public List<ComboItem> obtenerUsuariosAdmitenEnvio () throws PortafirmaFapServiceException;
}
