package services.filesystem;

import java.util.ArrayList;
import java.util.List;

import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;

import models.Agente;
import models.ResolucionFAP;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;

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
	public void eliminarSolicitudFirma() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String obtenerVersion() throws PortafirmaFapServiceException {
		return VERSION;
	}

	@Override
	public boolean comprobarSiResolucionFirmada(String idSolicitudFirma) throws PortafirmaFapServiceException {
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

	@Override
	public List<ComboItem> obtenerUsuariosAdmitenEnvio()
			throws PortafirmaFapServiceException {
		List<ComboItem> listaCombo = new ArrayList<ComboItem>();
		List<Agente> listaJefes = Agente.find("select agente from Agente agente").fetch();
		for (Agente agente: listaJefes) {
			listaCombo.add(new ComboItem(agente.username, agente.username+" - "+agente.name));
		}
		return listaCombo;
		
	}

	@Override
	public ObtenerEstadoSolicitudResponseType obtenerEstadoFirma(
			ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
