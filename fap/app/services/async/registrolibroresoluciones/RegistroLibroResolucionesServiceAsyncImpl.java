package services.async.registrolibroresoluciones;

import java.util.List;

import config.InjectorConfig;

import models.ResolucionFAP;
import play.libs.F.Promise;
import registroresolucion.AreaResolucion;
import registroresolucion.RecursoResolucion;
import registroresolucion.RegistroResolucion;
import registroresolucion.TipoResolucion;
import services.PublicarService;
import services.RegistroLibroResolucionesService;
import services.RegistroLibroResolucionesServiceException;
import services.async.GenericServiceAsyncImpl;
import services.async.RegistroLibroResolucionesServiceAsync;

public class RegistroLibroResolucionesServiceAsyncImpl extends GenericServiceAsyncImpl implements RegistroLibroResolucionesServiceAsync {
	
	static RegistroLibroResolucionesService registroLibroResolucionesService = InjectorConfig.getInjector().getInstance(RegistroLibroResolucionesService.class);

	@Override
	public Promise<Integer> mostrarInfoInyeccion() {
		return (Promise<Integer>) execute(registroLibroResolucionesService, "mostrarInfoInyeccion");
	}

	@Override
	public Promise<List<TipoResolucion>> leerTipos() throws RegistroLibroResolucionesServiceException {
		return (Promise<List<TipoResolucion>>) execute(registroLibroResolucionesService, "leerTipos");
	}

	@Override
	public Promise<List<AreaResolucion>> leerAreas() throws RegistroLibroResolucionesServiceException {
		return (Promise<List<AreaResolucion>>) execute(registroLibroResolucionesService, "leerAreas");
	}

	@Override
	public Promise<List<RecursoResolucion>> leerRecursos() throws RegistroLibroResolucionesServiceException {
		return (Promise<List<RecursoResolucion>>) execute(registroLibroResolucionesService, "leerRecursos");
	}

	@Override
	public Promise<RegistroResolucion> crearResolucion(ResolucionFAP resolucionFAP) throws RegistroLibroResolucionesServiceException {
    	Object[] params = {resolucionFAP};
		Class[] types = {ResolucionFAP.class};
		return (Promise<RegistroResolucion>) execute(registroLibroResolucionesService, "crearResolucion", params, types);
	}

}
