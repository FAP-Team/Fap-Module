package services.async.portafirma;

import java.io.InputStream;
import java.util.List;

import config.InjectorConfig;

import models.ResolucionFAP;
import play.libs.F.Promise;
import services.PortafirmaFapServiceException;
import services.PortafirmaFapService;
import services.async.GenericServiceAsyncImpl;
import services.async.PortafirmaServiceAsync;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;

public class PortafirmaServiceAsyncImpl extends GenericServiceAsyncImpl implements PortafirmaServiceAsync {
	
	static PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);

	@Override
	public Promise<Integer> mostrarInfoInyeccion() {
		return (Promise<Integer>) execute(portafirmaService, "mostrarInfoInyeccion");
	}

	@Override
	public Promise<String> obtenerVersion() {
		return (Promise<String>) execute(portafirmaService, "obtenerVersion");
	}

	@Override
	public Promise<PortafirmaCrearSolicitudResponse> crearSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
    	Object[] params = {resolucion};
		Class[] types = {ResolucionFAP.class};
		return (Promise<PortafirmaCrearSolicitudResponse>) execute(portafirmaService, "crearSolicitudFirma", params, types);
	}

	@Override
	public Promise<ObtenerEstadoSolicitudResponseType> obtenerEstadoFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		Object[] params = {resolucion};
		Class[] types = {ResolucionFAP.class};
		return (Promise<ObtenerEstadoSolicitudResponseType>) execute(portafirmaService, "obtenerEstadoFirma", params, types);
	}

	@Override
	public Promise<Integer> eliminarSolicitudFirma() throws PortafirmaFapServiceException {
		return (Promise<Integer>) execute(portafirmaService, "eliminarSolicitudFirma");
	}

	@Override
	public Promise<Boolean> comprobarSiResolucionFirmada(String idSolicitudFirma) throws PortafirmaFapServiceException {
		Object[] params = {idSolicitudFirma};
		Class[] types = {String.class};
		return (Promise<Boolean>) execute(portafirmaService, "comprobarSiResolucionFirmada", params, types);
	}

	@Override
	public Promise<List<ComboItem> > obtenerUsuariosAdmitenEnvio() throws PortafirmaFapServiceException {
		return (Promise<List<ComboItem> >) execute(portafirmaService, "obtenerUsuariosAdmitenEnvio");
	}

}
