package services.async.registro;

import config.InjectorConfig;
import models.Documento;
import models.ExpedientePlatino;
import models.JustificanteRegistro;
import models.ResolucionFAP;
import models.Solicitante;
import play.libs.F.Promise;
import services.RegistroLibroResolucionesService;
import services.RegistroService;
import services.RegistroServiceException;
import services.async.GenericServiceAsyncImpl;
import services.async.RegistroServiceAsync;

public class RegistroServiceAsyncImpl extends GenericServiceAsyncImpl implements RegistroServiceAsync {
	
	static RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);

	@Override
	public Promise<Boolean> isConfigured() {
		return (Promise<Boolean>) execute(registroService, "isConfigured");
	}

	@Override
	public Promise<Integer> mostrarInfoInyeccion() {
		return (Promise<Integer>) execute(registroService, "mostrarInfoInyeccion");
	}

	@Override
	public Promise<JustificanteRegistro> registrarEntrada(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion) throws RegistroServiceException {
    	Object[] params = {solicitante, documento, expediente, descripcion};
		Class[] types = {Solicitante.class, Documento.class, ExpedientePlatino.class, String.class};
		return (Promise<JustificanteRegistro>) execute(registroService, "registrarEntrada", params, types);
	}

	@Override
	public Promise<JustificanteRegistro> registroDeSalida(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion) throws RegistroServiceException {
    	Object[] params = {solicitante, documento, expediente, descripcion};
		Class[] types = {Solicitante.class, Documento.class, ExpedientePlatino.class, String.class};
		return (Promise<JustificanteRegistro>) execute(registroService, "registroDeSalida", params, types);
	}

}
