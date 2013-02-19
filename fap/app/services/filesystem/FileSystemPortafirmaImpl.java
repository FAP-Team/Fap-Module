package services.filesystem;

import models.ResolucionFAP;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;

public class FileSystemPortafirmaImpl implements PortafirmaFapService {
	
	final static String VERSION = "0.1";

	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException{
		// TODO: ¿Qué devolvemos en este mock?
		PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
		response.setIdSolicitud("fakeSolicitud");
		response.setComentarios("fakeComentarios");
		return response;
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
		return VERSION;
	}

	@Override
	public boolean comprobarSiResolucionFirmada(String idSolicitudFirma, String idAgente) throws PortafirmaFapServiceException {
		return true;
	}

	public boolean isConfigured() {
		// No necesita configuración
		return true;
	}
	
	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Portafirma ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Portafirma ha sido inyectado con FileSystem y NO está operativo.");
	}

}
