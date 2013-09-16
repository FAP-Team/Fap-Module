package services.async;

import java.util.List;

import play.libs.F.Promise;

import models.ResolucionFAP;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;

public interface PortafirmaServiceAsync {
	public Promise<Integer> mostrarInfoInyeccion();
	public Promise<String> obtenerVersion () throws PortafirmaFapServiceException; 
	public Promise<PortafirmaCrearSolicitudResponse> crearSolicitudFirma (ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public Promise<ObtenerEstadoSolicitudResponseType> obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException;
	public Promise<Integer> eliminarSolicitudFirma () throws PortafirmaFapServiceException;
	public Promise<Boolean> comprobarSiResolucionFirmada (String idSolicitudFirma) throws PortafirmaFapServiceException;
	public Promise<List<ComboItem> > obtenerUsuariosAdmitenEnvio () throws PortafirmaFapServiceException;
}
