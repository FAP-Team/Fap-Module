package services;

import java.util.List;

import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;

import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import models.ResolucionFAP;

public interface PortafirmaFapService {

	public void mostrarInfoInyeccion();
	public String obtenerVersion () throws PortafirmaFapServiceException; 
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public ObtenerEstadoSolicitudResponseType obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public void eliminarSolicitudFirma () throws PortafirmaFapServiceException;
	public boolean comprobarSiResolucionFirmada (String idSolicitudFirma) throws PortafirmaFapServiceException;
	public List<ComboItem> obtenerUsuariosAdmitenEnvio () throws PortafirmaFapServiceException;
}
