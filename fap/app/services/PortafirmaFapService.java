package services;

import java.util.List;

import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import models.ResolucionFAP;

public interface PortafirmaFapService {

	public void mostrarInfoInyeccion();
	public String obtenerVersion () throws PortafirmaFapServiceException; 
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma (ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public void obtenerEstadoFirma () throws PortafirmaFapServiceException;
	public void eliminarSolicitudFirma () throws PortafirmaFapServiceException;
	public boolean comprobarSiResolucionFirmada (String idSolicitudFirma, String idAgente) throws PortafirmaFapServiceException;
	public List<ComboItem> obtenerUsuariosAdmitenEnvio () throws PortafirmaFapServiceException;
}
