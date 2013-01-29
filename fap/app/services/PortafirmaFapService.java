package services;

import services.responses.PortafirmaCrearSolicitudResponse;
import models.ResolucionFAP;

public interface PortafirmaFapService {

	public String obtenerVersion () throws PortafirmaFapServiceException; 
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public void obtenerEstadoFirma () throws PortafirmaFapServiceException;
	public void eliminarSolicitudFirma () throws PortafirmaFapServiceException;
	public boolean comprobarSiResolucionFirmada (String idSolicitudFirma) throws PortafirmaFapServiceException;
	
}
